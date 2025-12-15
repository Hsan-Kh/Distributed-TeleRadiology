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
    *   Module léger permettant d'interroger l'état du serveur (ex: nombre de tâches restantes) via le protocole standard CORBA.

---

## Prérequis Techniques

*   **Java Development Kit (JDK) :** Version 1.8 (Recommandée pour la compatibilité native CORBA/RMI).
*   **Middleware de Messages :** Apache ActiveMQ (Version 5.16.x ou supérieure).
*   **Gestionnaire de dépendances :** Maven (Architecture Multi-Modules).

## Installation et Exécution

### 1. Démarrage du Middleware
Avant de lancer l'application, assurez-vous qu'ActiveMQ est fonctionnel.
```bash
# Dans le dossier bin d'ActiveMQ
./activemq start
```

### 2. Compilation
À la racine du projet (dossier parent) :
```bash
mvn clean install
```

### 3. Lancement 
Pour observer le comportement distribué complet, lancez les composants dans l'ordre strict suivant :

1.  **Serveur Hospitalier (Module `server`) :**
    *   Classe : `server.HospitalServer`
    *   *Action :* Initialise le registre RMI, charge l'image, et active le module de monitoring CORBA.

2.  **Dashboard Médecin (Module `client`) :**
    *   Classe : `client.DoctorClient`
    *   *Action :* Ouvre une fenêtre graphique en attente de flux JMS.

3.  **Workers (Module `worker`) :**
    *   Classe : `worker.WorkerNode`
    *   *Action :* Lancez-en **3 instances ou plus** en parallèle.
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

## Structure du Projet (Multi-Modules Maven)

Le projet est divisé en modules pour assurer une séparation propre des responsabilités :

*   `common` : Contient les Interfaces RMI (`ComputeService`) et les Objets Transférables (`ImageChunk`).
*   `server` : Contient le `HospitalServer`, le Registre RMI et l'implémentation CORBA.
*   `worker` : Contient le `WorkerNode` et la logique de calcul (Algorithme de Sobel).
*   `client` : Contient le `DoctorClient` (Swing + JMS) et le code CORBA généré.

---

**Auteurs :**
*   Hsan KHECHAREM
*   Moez JEDIDI
