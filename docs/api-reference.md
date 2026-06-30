# Référence API — Alumni ENSPM (backend Laravel/Sanctum)

Base URL : `https://api-alumni.enspm.net/api`
Auth : Bearer token (Sanctum), header `Authorization: Bearer {token}` sur toutes
les routes sauf `/login`, `/forgot-password`, `/verify-otp`, `/resend-otp`,
`/reset-password`.

Enveloppe de réponse systématique :
`{ type, status, message, data }` — `type` ∈ {"success","error","validation_error"}.
Erreurs de validation : `data` = objet `{ champ: "message" }`.

## Auth
- `POST /login` { email, password } → data.user, data.token
- `POST /logout`
- `POST /update-password`
- `POST /forgot-password` { email } → envoie OTP
- `POST /verify-otp` { email, otp }
- `POST /resend-otp` { email }
- `POST /reset-password` { email, otp, password, password_confirmation }
- `GET /user` → utilisateur courant

## Fil d'actualité (Posts)
- `GET /posts?per_page=15&page=N` → data.posts[], data.pagination
- `GET /posts/{id}`
- `POST /posts` (multipart : content, medias[])
- `POST /posts/{id}/update`
- `GET /posts/{id}/delete`
- `GET /posts/{id}/comments` , `GET /posts/{id}/likes`

## Commentaires / Likes (génériques, type = post|internship|training|job_offer)
- `POST /comments` { commentable_type, commentable_id, content }
- `POST /comments/update` , `DELETE /comments/{id}`
- `POST /likes/toggle` { likeable_type, likeable_id }

## Annonces
- `GET /announcements`

## Stages / Formations / Offres d'emploi (même pattern x3)
- `GET /internships` , `GET /internships/{id}` , `POST /internships`,
  `POST /internships/{id}/update` , `GET /internships/{id}/unpublish`,
  `GET /internships/draft`
- Idem `/trainings/*`
- `GET /job-offers`, `GET /job-offers/{id}` (show = `showOffer`),
  `POST /job-offers`, `POST /job-offers/{id}/update`,
  `GET /job-offers/{id}/suspend`

## Profil
- `GET /profile` , `POST /profile/update` , `POST /profile/photo` (multipart)
- `POST /profile/academics[, /update, /delete]`
- `POST /profile/experiences[, /update, /delete]`
- `POST /profile/interests[, /update, /delete]`
- `POST /profile/skills[, /update, /delete]`

## Réseau / Annuaire
- `GET /networking/members`
- `GET /networking/members/{id}`

## Organisations
- `GET /organisations` , `GET /organisations/{id}`
- `POST /organisations/{id}/follow` , `GET /organisations/{id}/followers`

## Notifications
- `GET /notifications` , `GET /notifications/unread-count`
- `POST /notifications/mark-all-as-read` , `POST /notifications/{id}/mark-as-read`
- `DELETE /notifications/{id}`

## Signalements (report)
- `POST /signalements` , `GET /signalements`

## Messagerie (optionnel V1, voir Phase 5)
- `GET /chats` , `POST /chats` (initier)
- `GET /group-chats` , `POST /group-chats` (+ add/remove-members)
- `GET /load-chat/{chat_id}` , `POST /create_message`

## Forums (hors périmètre V1)
- `GET /forums` , `POST /forums` , `GET /forums/{id}` , `GET /forums/{id}/posts`,
  `POST /forums/{id}/posts`
