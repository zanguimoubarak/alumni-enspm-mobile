# PLAN.md — Roadmap Alumni ENSPM Mobile

Ce plan découpe l'application Android native en phases livrables. Chaque phase doit rester compatible avec l'esprit "Lite" : UI simple, faible coût réseau, cache local, pagination stricte selon `current_page` / `last_page`, et aucune duplication de logique métier déjà portée par l'API Laravel/Sanctum.

## Principes transverses

- **Architecture cible** : MVVM dans `:app`, packages par domaine (`core`, `auth`, `feed`, `profile`, `networking`, `opportunities`, `notifications`, `messages`). Si le projet grossit, ces packages pourront devenir des modules Gradle dédiés sans changer les contrats.
- **Chaîne de données standard** : UI Compose → ViewModel exposant un `StateFlow<UiState>` → Repository → API Retrofit + DAO Room + DataStore si nécessaire.
- **Format API obligatoire** : tous les DTO réseau doivent parser l'enveloppe `{ type, status, message, data }` et propager `message` / erreurs de validation à l'UI.
- **Pagination obligatoire** : les écrans liste utilisent uniquement `data.pagination.current_page` et `data.pagination.last_page` pour charger la page suivante.
- **Offline-first** : afficher le cache Room immédiatement, rafraîchir en arrière-plan, conserver une lecture utile hors réseau, et ne jamais bloquer l'utilisateur avec un plein écran vide si des données locales existent.
- **Images Lite** : thumbnails ou formats compressés en liste, plein écran seulement après action utilisateur, et désactivation de l'auto-chargement quand le mode économie de données est actif.

## Phase 0 — Socle projet, design system et navigation shell

### 1. Objectif fonctionnel
Mettre en place un socle Android natif compilable : thème Compose clair/sombre, navigation principale vide, écran de login/OTP placeholder, configuration réseau et persistance prêtes pour les phases métier.

### 2. Modules / packages concernés
- `core` : thème, composants UI partagés, navigation, erreurs, utilitaires pagination.
- `core.networking` : Retrofit, OkHttp, intercepteurs, enveloppe API générique.
- `core.datastore` : préférences globales, token, économie de données.
- `core.database` : base Room initiale et migrations.
- `auth` : routes et écrans placeholders login / OTP.

### 3. Architecture prévue
- **UI Compose** : `MainActivity` héberge un `NavHost` avec shell bas : Accueil, Réseau, Opportunités, Notifications, Profil.
- **ViewModel `StateFlow`** : ViewModel de session exposant l'état `Loading`, `Authenticated`, `Unauthenticated`.
- **Repository** : `SessionRepository` lit le token DataStore et centralise logout.
- **API Retrofit** : client global avec `Accept: application/json` et intercepteur Bearer.
- **Cache Room** : base vide créée pour accueillir les entités des phases suivantes.
- **DataStore** : stockage du token Sanctum, préférences de thème et économie de données.

### 4. Offline-first et pagination
- Aucun écran métier complet dans cette phase, mais fournir les abstractions `PaginationState`, `PagedRepository` et conventions DAO pour `current_page` / `last_page`.
- Prévoir que les onglets liste affichent un état vide local plutôt qu'une erreur bloquante.

### 5. Risques techniques
- **Enveloppe API** : risque d'oublier le wrapper `{ type, status, message, data }` dans les premiers DTO ; prévoir un modèle générique `ApiEnvelope<T>`.
- **Token Sanctum** : DataStore Preferences n'est pas chiffré par défaut ; documenter la stratégie de durcissement pour Phase 1.
- **401 global** : l'intercepteur doit éviter les boucles de logout/navigation.
- **Taille APK** : dépendances initiales à limiter à la stack imposée.
- **Offline** : base Room et stratégies de cache doivent être posées dès le départ.
- **Thumbnails / économie de données** : prévoir un flag global avant de créer les listes.

### 6. Vérification manuelle
- Lancer l'app et vérifier que les 5 onglets de navigation répondent.
- Basculer thème système clair/sombre et vérifier l'application du thème.
- Vérifier que `BuildConfig.API_BASE_URL` pointe par défaut vers `https://api-alumni.enspm.net/api`.
- Vérifier qu'aucun écran métier complet n'a été ajouté prématurément.

## Phase 1 — Authentification et session persistante

### 1. Objectif fonctionnel
Implémenter login, logout, mot de passe oublié avec OTP, session persistante et déconnexion automatique sur 401.

### 2. Modules / packages concernés
- `auth` : UI, ViewModel, Repository, DTO auth.
- `core.networking` : intercepteur Bearer, intercepteur 401, parsing erreurs.
- `core.datastore` : stockage token et utilisateur minimal.
- `core.security` : durcissement du stockage token si nécessaire.

### 3. Architecture prévue
- **UI Compose** : écrans login, forgot-password, verify-otp, reset-password, composants de formulaire réutilisables.
- **ViewModel `StateFlow`** : `AuthUiState` pour champs, loading, erreurs API et navigation.
- **Repository** : `AuthRepository` appelle Retrofit, extrait `data.token`, persiste la session et expose logout.
- **API Retrofit** : `AuthApi` pour `/login`, `/forgot-password`, `/verify-otp`, `/resend-otp`, `/reset-password`, `/logout`, `/user`.
- **Cache Room** : pas obligatoire pour auth, sauf cache minimal du profil utilisateur si utile.
- **DataStore** : token Sanctum, identifiant utilisateur, éventuel timestamp de session.

### 4. Offline-first et pagination
- Auth nécessite le réseau pour login/OTP, mais l'état de session local doit être disponible au démarrage.
- Pas de pagination dans cette phase.

### 5. Risques techniques
- **Enveloppe API** : `validation_error` doit afficher les erreurs par champ.
- **Token Sanctum** : éviter logs, screenshots de debug et stockage en clair hors DataStore ; envisager chiffrement Android Keystore si demandé.
- **401 global** : une réponse 401 doit supprimer le token, invalider la session et renvoyer au login.
- **Taille APK** : ne pas ajouter de SDK auth externe lourd.
- **Offline** : démarrage offline avec token existant doit afficher le shell et laisser les écrans lire leur cache.
- **Thumbnails / économie de données** : sans impact direct, mais le flag DataStore reste disponible.

### 6. Vérification manuelle
- Tester login valide et invalide.
- Tester le flux forgot-password → OTP → reset-password, avec resend et cooldown.
- Redémarrer l'app après login et vérifier l'accès direct au shell.
- Simuler une réponse 401 et vérifier le logout global.
- Vérifier qu'aucun token n'apparaît dans les logs.

## Phase 2 — Fil d'actualité, likes et commentaires

### 1. Objectif fonctionnel
Créer le fil principal paginé, offline-first, avec détails de publication, likes optimistes et commentaires simples.

### 2. Modules / packages concernés
- `feed` : UI, ViewModel, Repository, DTO, entités Room.
- `core.database` : DAO posts, comments, pagination metadata.
- `core.networking` : `PostsApi`, `CommentsApi`, `LikesApi`.
- `core.ui` : cartes plates, skeletons, composant image thumbnail.

### 3. Architecture prévue
- **UI Compose** : liste LazyColumn, carte `enspm-card`, skeletons, écran détail, viewer image plein écran après tap.
- **ViewModel `StateFlow`** : `FeedUiState` avec cache, statut refresh, erreurs non bloquantes et état pagination.
- **Repository** : stratégie stale-while-revalidate : lire Room, appeler API, mettre à jour Room, exposer Flow.
- **API Retrofit** : `/posts?per_page=15&page=N`, `/posts/{id}`, `/comments`, `/likes/toggle`.
- **Cache Room** : posts, médias, commentaires, statut like local, métadonnées de pages.
- **DataStore** : lecture du mode économie de données pour bloquer les images automatiques.

### 4. Offline-first et pagination
- Au lancement : Room d'abord, refresh page 1 ensuite si réseau disponible.
- Infinite scroll : charger page `current_page + 1` uniquement si `current_page < last_page`.
- En cas d'échec réseau, conserver le cache et afficher un message léger.

### 5. Risques techniques
- **Enveloppe API** : `data.posts` et `data.pagination` doivent être mappés strictement.
- **Token Sanctum** : likes/commentaires nécessitent Bearer et doivent respecter le logout 401.
- **401 global** : rollback des actions optimistes si déconnexion.
- **Taille APK** : ne pas ajouter de lib shimmer lourde ; skeleton Compose maison.
- **Offline** : éviter d'écraser le cache avec une réponse partielle ou erreur.
- **Thumbnails / économie de données** : ne jamais charger l'image pleine résolution en liste ; placeholder cliquable si économie de données active.

### 6. Vérification manuelle
- Ouvrir le fil online, puis relancer en mode avion et vérifier l'affichage du cache.
- Scroller jusqu'à `last_page` et vérifier qu'aucun chargement supplémentaire n'est lancé.
- Tester like avec réseau puis couper le réseau pendant un like et vérifier rollback.
- Ajouter un commentaire et vérifier sa persistance après changement d'écran.
- Vérifier que les images de liste sont des thumbnails ou placeholders.

## Phase 3 — Profil et réseau alumni

### 1. Objectif fonctionnel
Implémenter le profil personnel éditable et l'annuaire alumni paginé avec profils publics en lecture seule.

### 2. Modules / packages concernés
- `profile` : UI profil, édition, Repository, DTO, cache Room.
- `networking` : annuaire, profil public, Repository, DTO, cache Room.
- `core.ui` : sections, labels majuscules, champs arrondis.
- `core.database` : entités profil, academics, experiences, skills, interests, members.

### 3. Architecture prévue
- **UI Compose** : écran profil avec sections éditables, formulaires bottom sheet ou écrans dédiés, annuaire LazyColumn.
- **ViewModel `StateFlow`** : `ProfileUiState` et `MembersUiState` séparés.
- **Repository** : profil : API comme source de vérité avec cache local ; annuaire : Room d'abord puis pagination réseau.
- **API Retrofit** : `/profile`, `/profile/update`, `/profile/photo`, sous-sections profil, `/networking/members`, `/networking/members/{id}`.
- **Cache Room** : profil courant, sous-sections, membres paginés, détails publics.
- **DataStore** : mode économie de données pour photos de profil en liste.

### 4. Offline-first et pagination
- Profil : afficher la dernière version Room offline, marquer les actions d'édition comme nécessitant réseau si pas de synchronisation différée.
- Annuaire : pagination stricte avec `current_page` / `last_page`, cache consultable offline.

### 5. Risques techniques
- **Enveloppe API** : chaque sous-section peut avoir une forme `data` différente ; créer des DTO dédiés.
- **Token Sanctum** : upload photo et édition doivent rester authentifiés.
- **401 global** : sauvegarder les champs en cours avant redirection login si possible.
- **Taille APK** : pas de cropper photo lourd sans validation explicite.
- **Offline** : éditions offline non synchronisées peuvent créer de la confusion ; privilégier lecture offline en V1.
- **Thumbnails / économie de données** : photos membres en liste désactivées si économie de données active.

### 6. Vérification manuelle
- Modifier infos générales, académique, expérience, intérêts et compétences, puis relancer l'app.
- Tester upload photo avec une image raisonnable.
- Parcourir l'annuaire jusqu'à la dernière page.
- Ouvrir un profil public offline après l'avoir déjà consulté.
- Vérifier les labels de section et cartes plates.

## Phase 4 — Opportunités, annonces et organisations

### 1. Objectif fonctionnel
Implémenter stages, formations, offres d'emploi, annonces et organisations avec follow/unfollow.

### 2. Modules / packages concernés
- `opportunities` : listes mutualisées internships/trainings/job-offers, détails, Repository.
- `announcements` : flux annonces.
- `organisations` : liste/détail/follow.
- `core.interactions` : commentaires/likes génériques réutilisés.
- `core.database` : entités opportunités, annonces, organisations, pagination.

### 3. Architecture prévue
- **UI Compose** : cartes mutualisées, onglets ou filtres par type, détail opportunité, page organisation.
- **ViewModel `StateFlow`** : un état générique `PagedListUiState<T>` pour réduire la duplication.
- **Repository** : repositories par ressource, mais helpers partagés pour pagination/cache.
- **API Retrofit** : `/internships`, `/trainings`, `/job-offers`, `/announcements`, `/organisations` et `/organisations/{id}/follow`.
- **Cache Room** : tables par type avec métadonnées communes.
- **DataStore** : économie de données pour logos/images.

### 4. Offline-first et pagination
- Toutes les listes suivent stale-while-revalidate.
- Chaque type conserve ses propres `current_page` / `last_page` pour éviter de mélanger les paginations.
- Les détails déjà ouverts restent disponibles offline.

### 5. Risques techniques
- **Enveloppe API** : ressources différentes sous `data.<ressource>` ; mapper explicitement chaque clé.
- **Token Sanctum** : création/update opportunités si activés doivent être authentifiés.
- **401 global** : follow/unfollow optimiste doit rollback si 401.
- **Taille APK** : mutualiser les composants au lieu d'ajouter des dépendances.
- **Offline** : ne pas afficher des boutons d'action comme réussis si réseau absent.
- **Thumbnails / économie de données** : logos/images seulement en basse résolution ou placeholder.

### 6. Vérification manuelle
- Parcourir stages, formations et emplois jusqu'à la dernière page.
- Ouvrir un détail déjà chargé en mode avion.
- Tester commentaires/likes génériques sur opportunité si activés.
- Tester follow/unfollow organisation avec succès et rollback sur échec réseau.
- Vérifier que le code mutualisé couvre les trois types d'opportunités.

## Phase 5 — Notifications et messagerie individuelle

### 1. Objectif fonctionnel
Ajouter notifications avec badge non lu et messagerie individuelle minimale. Forums et groupes restent hors périmètre sans validation explicite.

### 2. Modules / packages concernés
- `notifications` : liste, badge, mark-as-read, Repository, cache.
- `messages` : conversations 1-à-1, fil messages, envoi texte.
- `core.networking` : APIs notifications/chats.
- `core.database` : notifications, conversations, messages.
- `core.datastore` : préférences de polling si nécessaire.

### 3. Architecture prévue
- **UI Compose** : onglet notifications avec badge, liste notifications, conversation simple type texte.
- **ViewModel `StateFlow`** : `NotificationsUiState`, `UnreadCountState`, `ConversationUiState`.
- **Repository** : notifications Room + API ; messages avec refresh/polling pendant écran actif.
- **API Retrofit** : `/notifications`, `/notifications/unread-count`, mark read/delete, `/chats`, `/load-chat/{chat_id}`, `/create_message`.
- **Cache Room** : notifications paginées, conversations, messages récents.
- **DataStore** : économie de données sans impact image majeur, éventuel paramètre polling.

### 4. Offline-first et pagination
- Notifications : afficher cache puis refresh ; pagination par `current_page` / `last_page` si l'API pagine.
- Messages : derniers messages consultés lisibles offline ; envoi offline non prioritaire en V1.
- Polling raisonnable seulement sur écran actif pour limiter batterie/data.

### 5. Risques techniques
- **Enveloppe API** : unread-count peut avoir une forme `data` spécifique ; mapper explicitement.
- **Token Sanctum** : toutes les routes sont authentifiées.
- **401 global** : interrompre polling et rediriger login.
- **Taille APK** : pas de WebSocket ou SDK temps réel lourd sans besoin backend.
- **Offline** : éviter de promettre l'envoi offline si non implémenté.
- **Thumbnails / économie de données** : avatars en liste désactivés ou placeholders si économie active.

### 6. Vérification manuelle
- Vérifier que le badge non lu correspond à l'API.
- Marquer une notification comme lue et tout marquer comme lu.
- Ouvrir une conversation, envoyer un message texte et vérifier le refresh.
- Couper le réseau et vérifier que notifications/messages déjà vus restent lisibles.
- Confirmer qu'aucun forum ni chat de groupe n'a été implémenté sans demande.

## Phase 6 — Polish Lite, économie de données et performance

### 1. Objectif fonctionnel
Finaliser l'expérience Lite : réduire coûts réseau/mémoire, valider offline, activer économie de données et auditer la taille APK.

### 2. Modules / packages concernés
- `core.settings` : écran Paramètres et préférences.
- `core.ui` : skeletons, placeholders images, accessibilité.
- `core.image` : configuration Coil globale mémoire/disque.
- Tous les packages liste : `feed`, `networking`, `opportunities`, `notifications`, `messages`.

### 3. Architecture prévue
- **UI Compose** : écran Paramètres, toggle économie de données, placeholders cliquables, skeletons cohérents.
- **ViewModel `StateFlow`** : `SettingsUiState` et observation globale DataStore.
- **Repository** : aucun nouveau repository lourd, uniquement réglages et audit des repositories existants.
- **API Retrofit** : vérifier `per_page` raisonnable et absence de préchargement excessif.
- **Cache Room** : vérifier migrations, limites de cache et nettoyage si nécessaire.
- **DataStore** : préférence économie de données, éventuel thème, taille cache si exposée.

### 4. Offline-first et pagination
- Revalider chaque liste : cache affiché offline, refresh non bloquant, pagination arrêtée à `last_page`.
- Aucun écran liste ne doit charger toutes les pages en une fois.

### 5. Risques techniques
- **Enveloppe API** : vérifier que tous les mappers utilisent le même traitement d'erreur.
- **Token Sanctum** : audit logs et crash reports pour éviter toute fuite.
- **401 global** : vérifier comportement depuis chaque écran.
- **Taille APK** : supprimer dépendances inutilisées et générer rapport avant/après.
- **Offline** : corriger écrans vides ou erreurs bloquantes.
- **Thumbnails / économie de données** : tester que le mode coupe vraiment les images automatiques en liste.

### 6. Vérification manuelle
- Comparer taille APK debug/release avant et après nettoyage.
- Activer économie de données et vérifier feed + annuaire au minimum.
- Tester chaque liste en mode avion après un premier chargement online.
- Vérifier contraste thème sombre, tailles de texte et zones tactiles.
- Inspecter les dépendances Gradle et justifier tout ajout restant.
