Démonstration Caffeine avec Spring Boot
Présentation

Ce projet est une démonstration simple de l'utilisation de Caffeine comme système de cache dans une application Spring Boot.

L'objectif est de montrer concrètement la différence entre :

un premier appel où les données doivent être réellement récupérées ;

les appels suivants où le résultat peut être récupéré directement depuis le cache.

Aucune base de données n'est utilisée dans cette démonstration.

Les données sont simplement lues depuis un fichier texte :

src/main/resources/text.txt


Une interface HTML permet de lancer deux requêtes et d'afficher leurs temps d'exécution afin de comparer le premier appel avec l'appel suivant.

Technologies utilisées

Java

Spring Boot

Spring Web

Spring Cache

Caffeine

Maven

HTML

CSS

JavaScript

Architecture

Le fonctionnement général de l'application est le suivant :

                    Navigateur
                        |
                        | clic sur le bouton
                        v
                    index.html
                        |
                +-------+-------+
                |               |
           Requête 1       Requête 2
                |               |
                v               v
             GET /text       GET /text
                |               |
                v               v
         TextController   TextController
                |               |
                +-------+-------+
                        |
                        v
                   TextService
                        |
                   @Cacheable
                        |
                 +------+------+
                 |             |
               MISS           HIT
                 |             |
                 v             v
             text.txt      Caffeine
                 |             |
                 +------+------+
                        |
                        v
                    résultat


Lors du premier appel, le cache ne contient pas encore le résultat.

La méthode getText() est donc réellement exécutée et le fichier text.txt est lu.

Lors du deuxième appel, le résultat est déjà présent dans Caffeine.

La méthode n'a donc pas besoin de relire le fichier.

Structure du projet
demo/
|
+-- pom.xml
|
+-- src/
    +-- main/
        +-- java/
        |   +-- com/
        |       +-- demo_caffeine/
        |           +-- demo/
        |               |
        |               +-- DemoApplication.java
        |               +-- TextService.java
        |               +-- TextControler.java
        |               +-- TextResult.java
        |
        +-- resources/
            |
            +-- text.txt
            |
            +-- static/
                +-- index.html

Dépendances Maven

Le projet utilise notamment les dépendances suivantes :

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

Spring Web

spring-boot-starter-web permet de créer le serveur HTTP et les endpoints REST.

Il permet notamment d'utiliser :

@RestController


et :

@GetMapping


Dans notre application, le endpoint principal est :

GET /text

Spring Cache

spring-boot-starter-cache fournit l'intégration du système de cache de Spring.

Il permet notamment d'utiliser :

@EnableCaching


et :

@Cacheable

Caffeine

Caffeine est la bibliothèque utilisée comme moteur de cache.

Spring Cache fournit l'abstraction permettant de manipuler le cache, tandis que Caffeine fournit l'implémentation du cache.

Lancement de l'application

Le projet utilise Maven.

L'application peut être lancée depuis Eclipse en exécutant :

DemoApplication.java


Spring Boot démarre alors le serveur web intégré.

Par défaut, l'application est accessible à l'adresse :

http://localhost:8080


La page principale est accessible à :

http://localhost:8080/

Le fichier text.txt

Le fichier utilisé pour la démonstration se trouve ici :

src/main/resources/text.txt


Par exemple :

Hello Caffeine !


Le fichier est volontairement simple.

L'objectif n'est pas de démontrer la lecture d'un fichier, mais de montrer qu'une opération peut être exécutée une première fois puis évitée grâce au cache.

Le même principe pourrait être appliqué à :

une requête vers une base de données ;

un appel à une API externe ;

un calcul coûteux ;

une lecture de fichier ;

un appel à un autre service.

TextService

TextService est responsable de la lecture du fichier.

Exemple :

@Service
public class TextService {

    @Cacheable("texts")
    public String getText() throws IOException {

        System.out.println("[*] Lecture du fichier");

        try (InputStream inputStream =
                     getClass().getClassLoader()
                     .getResourceAsStream("text.txt")) {

            if (inputStream == null) {
                throw new IOException("Le fichier text.txt est introuvable");
            }

            return new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
            );
        }
    }
}


La ligne importante est :

@Cacheable("texts")


Elle indique à Spring que le résultat de cette méthode doit être mis en cache.

Le cache utilisé porte le nom :

texts

Fonctionnement de @Cacheable
Premier appel

Lors du premier appel, le cache est vide.

On obtient donc un cache miss.

GET /text
    |
    v
TextService.getText()
    |
    v
Cache "texts"
    |
    v
MISS
    |
    v
Lecture de text.txt
    |
    v
Résultat
    |
    v
Caffeine


Le résultat est alors stocké dans le cache.

Deuxième appel

Lors du deuxième appel :

GET /text
    |
    v
TextService.getText()
    |
    v
Cache "texts"
    |
    v
HIT
    |
    v
Résultat déjà présent


La méthode n'est donc pas exécutée une deuxième fois.

Le fichier n'est pas relu.

Activation du cache

Dans DemoApplication.java, le système de cache est activé avec :

@EnableCaching
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}


@EnableCaching indique à Spring d'activer son mécanisme de cache.

TextController

Le Controller expose le endpoint :

GET /text


Il mesure également le temps nécessaire à l'appel du service.

Exemple :

@GetMapping("/text")
public TextResult getText() throws IOException {

    long debut = System.nanoTime();

    String text = ts.getText();

    long fin = System.nanoTime();

    long temps = fin - debut;

    System.out.println(
        "[+] Elapsed Time: " + temps + " ns"
    );

    return new TextResult(text, temps);
}

Mesure du temps

La mesure est réalisée avec :

System.nanoTime()


Avant l'appel :

long debut = System.nanoTime();


Après l'appel :

long fin = System.nanoTime();


La différence est ensuite calculée :

long temps = fin - debut;


Le résultat est exprimé en nanosecondes :

ns


Par exemple :

7426789 ns


correspond approximativement à :

7,43 ms


La mesure concerne l'appel au service depuis le Controller.

Elle ne représente donc pas uniquement le temps de lecture du fichier.

TextResult

Le Controller retourne un objet contenant le texte et le temps d'exécution :

public class TextResult {

    private String text;
    private long timeNs;

    public TextResult(String text, long timeNs) {
        this.text = text;
        this.timeNs = timeNs;
    }

    public String getText() {
        return text;
    }

    public long getTimeNs() {
        return timeNs;
    }
}


Spring transforme automatiquement cet objet en JSON.

Exemple :

{
    "text": "Hello Caffeine !",
    "timeNs": 108757
}

Interface HTML

L'interface se trouve dans :

src/main/resources/static/index.html


Spring Boot sert automatiquement les fichiers présents dans le dossier :

src/main/resources/static/


La page contient un bouton permettant de lancer la démonstration.

Lorsque l'utilisateur clique sur le bouton, JavaScript effectue deux appels au endpoint :

GET /text


Les deux résultats sont ensuite affichés dans deux blocs différents.

Déroulement de la démonstration

Lors du clic sur le bouton :

                  Clic sur le bouton
                          |
                          v
                   Première requête
                          |
                          v
                       GET /text
                          |
                          v
                      @Cacheable
                          |
                        MISS
                          |
                          v
                  Lecture du fichier
                          |
                          v
                       Caffeine
                          |
                          v
                    Résultat 1


Puis :

                   Deuxième requête
                          |
                          v
                       GET /text
                          |
                          v
                      @Cacheable
                          |
                         HIT
                          |
                          v
                       Caffeine
                          |
                          v
                    Résultat 2


Les deux temps sont ensuite affichés côte à côte dans la page HTML.

Exemple de résultat

Lors d'un test, des mesures similaires aux suivantes ont été obtenues :

[*] Lecture du fichier

[+] Elapsed Time: 7426789 ns
[+] Elapsed Time: 653173 ns
[+] Elapsed Time: 109799 ns
[+] Elapsed Time: 108757 ns
[+] Elapsed Time: 155348 ns


Dans cette série :

Premier appel : 7 426 789 ns
Quatrième appel : 108 757 ns


Le quatrième appel était donc environ 68 fois plus rapide que le premier appel dans cette mesure précise.

Il ne faut cependant pas considérer ce facteur comme une performance générale de Caffeine.

Les performances dépendent notamment :

de la taille des données ;

du coût de l'opération originale ;

de la JVM ;

du système d'exploitation ;

du matériel ;

de la charge de l'application ;

de la méthode de mesure.

L'objectif de la démonstration est principalement de montrer que le résultat peut être réutilisé depuis le cache et que l'opération originale n'est pas répétée lors d'un cache hit.

Vérification du fonctionnement du cache

Pour vérifier que Caffeine fonctionne réellement, le service affiche :

[*] Lecture du fichier


juste avant de lire le fichier.

Lors de plusieurs appels, on peut obtenir :

[*] Lecture du fichier
[+] Elapsed Time: ...
[+] Elapsed Time: ...
[+] Elapsed Time: ...
[+] Elapsed Time: ...


Le message :

[*] Lecture du fichier


n'apparaît qu'une seule fois.

Cela permet de vérifier que :

Premier appel
    |
    v
Méthode exécutée
    |
    v
Fichier lu
    |
    v
Résultat placé dans le cache


Puis :

Appels suivants
    |
    v
Résultat récupéré depuis Caffeine
    |
    v
Fichier non relu


Cette vérification est plus importante que la simple comparaison des temps.

Expiration du cache

Dans la configuration actuelle, aucune durée d'expiration spécifique n'est configurée.

Il est cependant possible de configurer Caffeine avec différentes politiques d'expiration.

Par exemple :

expireAfterAccess(10, TimeUnit.SECONDS)


signifie qu'une entrée peut expirer après une période de 10 secondes sans accès.

Une autre possibilité est :

expireAfterWrite(10, TimeUnit.SECONDS)


qui définit une expiration basée sur le temps écoulé depuis l'écriture de l'entrée.

Ces politiques ne sont pas nécessaires pour la démonstration de base.

Scénario de test

Pour reproduire la démonstration :

Démarrer l'application Spring Boot.

Ouvrir :

http://localhost:8080/


Cliquer sur le bouton de lancement de la démonstration.

Observer les deux résultats affichés dans la page.

Observer la console Eclipse.

Vérifier que la lecture du fichier n'a lieu que lors du premier appel.

Comparer les temps affichés pour les deux requêtes.

Objectif pédagogique

Cette démonstration permet de comprendre plusieurs concepts.

Spring Boot

Spring Boot permet de créer rapidement une application Java avec un serveur web intégré.

Controller

Le Controller reçoit les requêtes HTTP :

GET /text

Service

Le Service contient la logique permettant de lire le fichier.

Spring Cache

Spring fournit une abstraction permettant de déclarer qu'une méthode doit être mise en cache.

Caffeine

Caffeine fournit le moteur de cache utilisé par l'application.

Cache Miss

Le résultat n'est pas présent dans le cache.

Cache
  |
  v
MISS
  |
  v
Exécution de la méthode
  |
  v
Résultat
  |
  v
Cache

Cache Hit

Le résultat est déjà présent dans le cache.

Cache
  |
  v
HIT
  |
  v
Résultat déjà disponible


La méthode originale n'a donc pas besoin d'être exécutée.

Conclusion

Cette application montre comment Caffeine peut éviter de répéter une opération coûteuse.

Dans cette démonstration, l'opération coûteuse est la lecture du fichier :

text.txt


Le même principe peut être utilisé pour mettre en cache :

des requêtes SQL ;

des appels à des API ;

des calculs coûteux ;

des fichiers ;

des appels à des services externes ;

des données fréquemment consultées.

Le principe général est :

Premier appel
    |
    v
Cache Miss
    |
    v
Exécution de l'opération
    |
    v
Résultat
    |
    v
Cache


Puis :

Appels suivants
    |
    v
Cache Hit
    |
    v
Résultat déjà disponible


L'intérêt du cache est donc de conserver temporairement le résultat d'une opération afin d'éviter de refaire inutilement cette opération lors des appels suivants.
