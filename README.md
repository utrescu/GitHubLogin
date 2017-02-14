# GitHubLogin
Exemple de ús de login social fent servir GitHub OAuth des de Spring Boot

L'he deixat tant senzill com he pogut perquè sigui fàcil d'entendre però que no sigui massa complicat afegir altres proveïdors:

1. S'afegeixen a application.yml
2. Es defineix un filtre a la classe WebSecurityConfig

A Github es crea una aplicació: 
![crear app](Imatges/crear-aplicacio.png)
i s'omple la configuració de l'aplicació. En el meu cas és com aquesta:

![github](Imatges/github.png)

Només cal copiar la clau i el secret que ha donat.
