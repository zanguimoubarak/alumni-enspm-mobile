# AGENTS.md — Alumni ENSPM Mobile

## Contexte produit
App mobile "Lite" pour la plateforme Alumni ENSP Maroua. Référence d'esprit produit :
Facebook Lite (légèreté, rapidité sur réseau faible, UI minimale, pas d'animations
superflues, listes texte-first avec images chargées en différé/basse résolution).
Elle consomme l'API existante du site web (Laravel/Sanctum) — ne jamais dupliquer
de logique métier déjà gérée côté backend.

## Stack imposée
- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- Hilt pour l'injection de dépendances
- Retrofit + OkHttp pour le réseau, Moshi ou kotlinx.serialization pour le JSON
- Room pour le cache offline (feed, profil, notifications)
- DataStore (Preferences) pour stocker le token Sanctum — jamais SharedPreferences brut
- Coil pour les images, avec `ImageLoader` configuré en cache disque + mémoire limité
  et chargement progressif (placeholder flou ou couleur dominante avant la vraie image)
- Architecture : MVVM, un seul Activity (ComponentActivity), Compose pour tous les écrans
- Pas de bibliothèque lourde non justifiée (pas de Glide+Coil en double, pas de
  framework UI alternatif, pas de WebView pour des écrans natifs)

## Authentification (API)
- Base URL : variable d'environnement / BuildConfig, valeur par défaut
  `https://api-alumni.enspm.net/api`
- Login : `POST /login` avec `{ email, password }` → réponse contient
  `data.token` (Sanctum plainTextToken) et `data.user`
- Toutes les requêtes authentifiées : header `Authorization: Bearer {token}`
  et `Accept: application/json`
- Pas de refresh token côté API actuelle : si le serveur renvoie 401, déconnecter
  l'utilisateur et le renvoyer à l'écran de connexion (intercepteur OkHttp dédié)
- Mot de passe oublié : `POST /forgot-password` → `POST /verify-otp` →
  `POST /reset-password`, avec `POST /resend-otp` si besoin

## Format des réponses API (toujours respecter, voir docs/api-reference.md)
Toutes les réponses suivent cette enveloppe :
```json
{ "type": "success" | "error" | "validation_error", "status": 200, "message": "...", "data": { } }
```
Les listes paginées renvoient `data.<ressource>` (tableau) + `data.pagination` :
`{ total, per_page, current_page, last_page }`. Toujours utiliser `current_page`/`last_page`
pour le scroll infini, jamais deviner un format de pagination différent.

## Design system (charte graphique du site web à reproduire fidèlement)
Source de vérité : `tailwind.config.js` + `src/index.css` du frontend web (couleurs HSL,
classes `.enspm-*`). Traduire en valeurs Compose équivalentes :

- Police : Roboto (toutes graisses 300–700) — charger via `res/font`
- Couleurs principales (light theme) :
  - `primary` = bleu `#2563EB` → `indigo` `#4F46E5` (le bouton primaire est un
    **gradient horizontal** bleu→indigo, jamais une couleur plate)
  - `background` = blanc, `foreground` = quasi-noir (`hsl(222.2 84% 4.9%)`)
  - `border` = gris très clair (`hsl(214.3 31.8% 91.4%)`), utilisé en bordure fine
    1dp sur les cartes, jamais d'ombre lourde par défaut
  - `muted-foreground` = gris moyen pour les textes secondaires/labels
- Dark theme : mêmes rôles sémantiques inversés, voir bloc `.dark` du CSS — implémenter
  un vrai thème sombre Compose (pas juste un overlay)
- Rayon de bordure de référence : `0.75rem` (~12dp) sur les inputs, jusqu'à 16dp sur les
  cartes (`enspm-card` = carte plate, fond blanc, bordure fine, coins très arrondis,
  PAS d'ombre portée par défaut)
- Boutons primaires = pill/rounded-xl, gradient bleu→indigo, lift léger au press
- Labels de section = majuscules, petite taille, `letter-spacing` large, gris clair
  (équivalent de `.enspm-label`)
- Barres de recherche = forme pill, fond gris très clair, bordure fine
- Globalement : esthétique "flat", pas de Material lourd par défaut — cartes plates,
  peu d'ombres, accents de couleur réservés aux actions principales et aux gradients
  de boutons/CTA

## Principes "Lite" (non négociables)
- Chaque écran liste (fil, annuaire, opportunités, notifications) doit paginer côté
  réseau ET mettre en cache local (Room) pour un affichage instantané hors-ligne/au
  prochain lancement
- Les images ne se chargent jamais en pleine résolution dans une liste : thumbnail
  compressée d'abord, plein écran seulement au tap
- Pas de chargement d'image automatique en liste si l'utilisateur a activé un mode
  "économie de données" (à prévoir dans Paramètres)
- Squelettes de chargement (shimmer/skeleton) plutôt que spinners plein écran
- Pas d'animations de transition coûteuses ; préférer des fades courts (<200ms)
- Taille d'APK et nombre de dépendances surveillés : justifier toute nouvelle lib
- Architecture offline-first : l'app doit rester utilisable (lecture) sans réseau

## Conventions de code
- Un module Gradle par grande feature si le projet grossit (sinon `:app` seul au
  début), packages `com.enspm.alumni.<feature>`
- ViewModel + StateFlow/UiState par écran, pas de logique réseau dans les Composables
- Tous les appels réseau passent par un `Repository`, jamais d'appel Retrofit direct
  depuis l'UI
- Tester au minimum : mapping JSON↔modèle, intercepteur d'auth, logique de pagination
- Commits en français, style conventional commits (`feat:`, `fix:`, `chore:`...)

## Hors périmètre (ne pas implémenter sans validation explicite)
- Tout l'espace Admin (`/admin/*` côté API) — c'est un outil web only
- Les forums et le chat de groupe sont optionnels en V1 "Lite" (voir Phase 5) — ne
  pas les construire avant que les phases 1 à 4 soient validées
