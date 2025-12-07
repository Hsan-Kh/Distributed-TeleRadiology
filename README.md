# Système Distribué de Télé-Radiologie

## Description du Projet
Ce projet a été réalisé dans le cadre du module de Développement d'Applications Réparties (DAR). Il s'agit d'une simulation d'une plateforme de **Télé-expertise médicale** basée sur une architecture de type "Grille de Calcul" (Grid Computing).

Le système permet de traiter des images médicales volumineuses (IRM/Radiographies) en distribuant la charge de calcul sur plusieurs nœuds (Workers) via le réseau. Le traitement appliqué est un filtre de **Détection de Contours (Sobel)**, utilisé pour mettre en évidence les structures anatomiques (fractures, tumeurs).

### Objectifs Académiques
Ce projet met en œuvre et valide les compétences suivantes :
*   **Java RMI :** Communication synchrone et transfert d'objets complexes (matrices de pixels).
*   **JMS (ActiveMQ) :** Communication asynchrone pour la visualisation temps réel.
*   **CORBA :** Interopérabilité pour le monitoring du système.
*   **Calcul Parallèle :** Découpage et traitement distribué de données.

---

## Architecture Logicielle

Le système repose sur une architecture hétérogène composée de quatre entités distinctes :

1.  **Serveur Hospitalier (Master / RMI Server)**
    *   Charge l'image médicale brute.
    *   Découpe l'image en segments horizontaux (*Chunks*).
    *   Expose une interface RMI `ComputeService` pour distribuer les tâches.
    *   Héberge un module CORBA pour le monitoring de l'état du serveur.

2.  **Nœuds de Calcul (Workers / RMI Clients)**
    *   Récupèrent dynamiquement les tâches auprès du serveur.
    *   Appliquent l'algorithme de filtre de Sobel (traitement matriciel).
    *   Renvoient le résultat (objet `ProcessedChunk`) au serveur.
    *   *Note : Plusieurs instances de Workers doivent être lancées en parallèle pour simuler la grille.*

3.  **Dashboard Médecin (Visualisation / JMS Client)**
    *   Interface graphique (Swing) totalement découplée du serveur de calcul.
    *   S'abonne au Topic JMS `TeleRadiology`.
    *   Reçoit et affiche les segments traités au fur et à mesure de leur disponibilité (Rendu Progressif).

4.  **Client de Supervision (CORBA Client)**
    *   Module léger permettant d'interroger l'état du serveur (ex: charge CPU, nombre de tâches restantes) via le protocole standard CORBA.

---

## Prérequis Techniques

*   **Java Development Kit (JDK) :** Version 1.8 (Recommandée pour la compatibilité native CORBA/RMI).
*   **Middleware de Messages :** Apache ActiveMQ (Version 5.16.x ou supérieure).
*   **Gestionnaire de dépendances :** Maven.

## Installation et Exécution

### 1. Démarrage du Middleware
Avant de lancer l'application, assurez-vous qu'ActiveMQ est fonctionnel.
```bash
# Dans le dossier bin d'ActiveMQ
./activemq start
```

### 2. Compilation
À la racine du projet :
```bash
mvn clean install
```

### 3. Lancement des Modules
Pour observer le comportement distribué complet, lancez les composants dans l'ordre strict suivant (via votre IDE ou terminal) :

1.  **Serveur Hospitalier :**
    *   Classe : `server.HospitalServer`
    *   *Action :* Initialise le registre RMI, charge l'image, et active le module de monitoring CORBA.

2.  **Dashboard Médecin :**
    *   Classe : `client.DoctorClient`
    *   *Action :* Ouvre une fenêtre graphique en attente de flux JMS.

3.  **Client de Supervision CORBA (Optionnel) :**
    *   Classe : `client.AdminConsole` 
    *   *Action :* Se connecte au serveur via l'IOR et affiche l'état du système.

4.  **Workers (Lancez-en 3 ou plus) :**
    *   Classe : `worker.WorkerNode`
    *   *Action :* Chaque worker commence à récupérer et traiter des segments.
    *   *Observation :* Le Dashboard se remplit de manière non-séquentielle (ex: la bande 5 arrive avant la bande 1), prouvant le parallélisme.

---

## Choix Technologiques et Justification

### Pourquoi RMI ?
Le protocole RMI a été privilégié pour la communication entre le Serveur et les Workers car il supporte nativement la **sérialisation d'objets Java**.
Contrairement aux Sockets bruts, RMI nous permet de transférer des instances de la classe `ImageChunk` (contenant les métadonnées de position et la matrice de pixels) de manière transparente et fortement typée, garantissant l'intégrité des données médicales.

### Pourquoi JMS ?
Le modèle **Publish-Subscribe** de JMS a été choisi pour le Dashboard afin d'assurer un **découplage temporel et spatial**.
Si le poste du médecin subit une latence réseau ou une déconnexion temporaire, cela ne bloque pas le processus de calcul côté serveur. De plus, cette architecture permettrait théoriquement de connecter plusieurs écrans de visualisation simultanément sans surcharge.

### Pourquoi CORBA ?
Un module CORBA a été intégré pour démontrer l'interopérabilité du système. Il expose une interface standardisée (IDL) permettant à des systèmes externes (Legacy, C++, Python) de superviser la charge du serveur Java sans dépendre de la JVM.

---

## Détails Algorithmiques

L'algorithme de **Filtre de Sobel** est utilisé pour la détection de contours.
*   Chaque Worker applique deux matrices de convolution (Gradient X et Gradient Y) sur chaque pixel du segment reçu.
*   **Artefacts de frontière :** L'image reconstruite présente des lignes noires horizontales entre les segments. Ceci est un comportement attendu et assumé dans ce prototype : les Workers traitant les segments de manière isolée (sans connaissance des pixels voisins du segment adjacent), l'algorithme ne peut calculer le gradient sur les bords extrêmes. Cela sert de preuve visuelle de la distribution stricte des tâches.

---

## Structure du Projet

*   `src/main/java/common` : Interfaces RMI et Objets Transférables (DTOs).
*   `src/main/java/server` : Implémentation du Serveur, RMI Registry, CORBA Servant.
*   `src/main/java/worker` : Logique client RMI et Algorithme de Sobel.
*   `src/main/java/client` : Interface graphique Swing, Consommateur JMS et Client CORBA.
*   `src/main/resources` : Fichiers images de test.
*   `src/main/idl` : Définition de l'interface CORBA.

---

**Auteurs :**
*   Hsan KHECHAREM
*   Mouez JEDIDI
*   *Faculté des Sciences de Sfax*
