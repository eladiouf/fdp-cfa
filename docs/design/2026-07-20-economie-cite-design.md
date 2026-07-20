# Économie de la Cité — Document de conception

> Mod custom NeoForge 1.21.1 pour le serveur RP « La Première Cité »
> (modpack *Create Ultimate Selection 2*, MC 1.21.1, NeoForge).
> Ce document est le **blueprint** : il décrit CE QU'ON CONSTRUIT et POURQUOI, pas encore le code ligne par ligne.

---

## 1. Vision

Le serveur est un isekai industriel : onze joueurs invoqués dans un monde oublié doivent fonder
la première grande ville. L'économie n'est **pas** le but du jeu — c'est le **liant social** :
elle force les joueurs à échanger, à se spécialiser, et donne du sens aux métiers (agriculteur,
mineur, cuisinier, bâtisseur…).

Objectif de l'économie :

- pousser les joueurs à **interagir** (vendre, acheter, se payer entre eux) ;
- récompenser le **travail utile à la ville** (contrats, commandes municipales) ;
- rester **simple** pour des débutants complets à Minecraft ;
- financer les **grands chantiers communs** via un Trésor alimenté par les taxes.

---

## 2. Principes économiques

### La monnaie

- **Émeraude = monnaie de référence** (comme l'or dans la vraie vie). Physique, rare, précieuse.
- **Crédit = argent virtuel** stocké à la banque, pour les petits montants et les paiements.
- **Taux fixe officiel : `1 émeraude = 100 crédits`.**
- La réserve de la banque = les émeraudes physiquement déposées (couverture 1:1). La banque ne
  crée jamais de crédits « à partir de rien ».

### Deux circuits d'argent (règle d'or de l'équilibre)

**🟡 Circuit 1 — Création de monnaie (le « robinet »)**
Seuls **deux** canaux font entrer des émeraudes **neuves** dans l'économie :
1. les **mines** (minerai d'émeraude) ;
2. les **contrats Bountiful** (payés en émeraudes).

**🟢 Circuit 2 — Circulation & redistribution (aucune création)**
Ne créent **aucune** émeraude — ils ne font que déplacer de l'argent existant :
- le **marché central**, les **boutiques perso** et les **paiements directs** (joueur → joueur) ;
- les **commandes municipales**, **payées par le Trésor** (donc par les taxes déjà collectées).
  Le Trésor est une **cagnotte de redistribution**, pas un robinet : s'il est vide, aucune
  commande n'est publiée (§3.6).

> Chaîne complète : mines/Bountiful → joueurs → marché → **taxes** → Trésor → **commandes
> municipales** → joueurs. Auto-équilibrée : la ville ne peut redistribuer que ce qu'elle a taxé.

**🔻 Les drains**
- Les **besoins des villageois** consomment des produits payés par le Trésor (l'argent ressort).
- Les **échanges villageois** où le joueur **achète** un objet contre des émeraudes retirent de
  la monnaie.
- Les **taxes de marché** transfèrent une part au Trésor (redistribuée ensuite en commandes).

> ⚠️ Sans contrôle des échanges villageois (Easy Villagers / Goblin Traders), les joueurs
> fabriqueraient des émeraudes à volonté et la monnaie s'effondrerait. Voir §7.

---

## 3. Composants du mod

Le mod regroupe six systèmes. Chacun est indépendant et testable séparément.

### 3.1 Banque + Villageois Banquier

- **Compte virtuel par joueur** (solde en crédits), persistant, sauvegardé côté serveur.
- **Villageois Banquier** : métier custom.
  - **Ne spawn jamais naturellement.** Un villageois devient Banquier uniquement si un joueur
    **fabrique et pose sa table de métier** (bloc « Table du Banquier »).
  - Interagir avec lui ouvre le menu de la banque.
- Opérations :
  - **Dépôt** : émeraudes physiques → crédits (`1 émeraude → 100 crédits`).
  - **Retrait** : crédits → émeraudes physiques (par tranche de 100). Les crédits en dessous de
    100 restent sur le compte.
  - **Consulter** le solde et l'**historique** des transactions.

### 3.2 Marché central + Villageois Marchand

- **Villageois Marchand** : métier custom.
  - **Ne spawn jamais naturellement.** Nécessite un bloc « Comptoir du Marchand » fabriqué et posé.
  - Situé dans le bâtiment du marché (choix des joueurs).
- **Place de marché joueur-à-joueur** :
  - un joueur met un objet en vente au **prix qu'il veut** (prix libre) ;
  - l'objet est mis en **dépôt (escrow) côté serveur** — retiré de l'inventaire, gardé en sûreté ;
  - un autre joueur l'achète, **même si le vendeur est déconnecté** ;
  - le paiement se fait en **crédits** (débité de l'acheteur, crédité au vendeur) ;
  - **on peut vendre n'importe quoi** (objet non demandé compris) ;
  - **taxe de 5 %** prélevée sur chaque vente → **Trésor de la Cité**.

### 3.3 Boutiques personnelles (bloc-boutique)

- **Bloc craftable** que chaque joueur pose où il veut (maison, quartier, place…).
- **Vente uniquement** (l'achat automatique par le bloc est une évolution future, hors périmètre).
- Le propriétaire **remplit le bloc** de son stock et fixe un **prix libre**.
- Un client interagit → paie en **crédits** → reçoit l'objet ; le compte du proprio est crédité,
  **même hors ligne** (le bloc garde le stock).
- **Taxe de 2 %** → Trésor de la Cité (plus faible qu'au marché central : récompense l'effort de
  construire une vraie boutique).
- Le bloc n'est utilisable que par son **propriétaire** pour le remplir/reprendre le stock.

### 3.4 Paiements directs entre joueurs

Deux méthodes, **même système sécurisé**, même historique :
1. **Carnet bancaire** (objet) : ouvre un menu, choisir le destinataire, saisir le montant,
   **confirmer** avant l'envoi. Méthode principale, adaptée aux débutants.
2. **Commande `/payer <joueur> <montant>`** : raccourci rapide.

- Fonctionne **même si le destinataire est déconnecté**.
- **Sans taxe** (transfert privé, pas une vente).
- Chaque opération apparaît dans l'**historique des deux comptes**.

### 3.5 Trésor de la Cité

- **Compte spécial** (pas un joueur) qui reçoit **toutes les taxes** (5 % marché, 2 % boutiques).
- Sert à **financer les commandes municipales** (§3.6).
- **Consultable** par tous ; **modifiable** seulement par les administrateurs / le rôle RP « Trésorier »
  (commandes admin pour ajuster taux de taxe et solde selon les lois RP).

### 3.6 Besoins de la population + Prospérité

Le système qui donne une « vie » économique à la ville sans MineColonies.

1. **Chaque semaine (de jeu)**, le Marchand publie automatiquement des **commandes municipales**
   selon le **nombre de villageois** : nourriture, laine/vêtements, outils, torches, bois, pierre,
   objets de confort…
2. Les joueurs **remplissent** ces commandes.
3. Le **Trésor les paie en crédits** (argent déjà taxé, pas d'émeraudes neuves créées).
4. Les produits livrés sont **consommés** par la population (drain).
5. Si les besoins sont satisfaits → la **Prospérité de la Cité** monte → **débloque** plus de
   commandes, de meilleures récompenses, de nouvelles capacités commerciales.
6. Si le **Trésor est vide** → **plus de commandes** publiées : aucune monnaie n'est créée
   gratuitement.

> Les villageois ne construisent rien eux-mêmes : ils créent une **demande économique permanente**.
> Ce sont toujours les joueurs qui bâtissent et font évoluer la ville.

---

## 4. Récapitulatif marché

| | Marché central (Marchand) | Boutique perso (bloc) |
|---|---|---|
| Emplacement | Bâtiment du marché | Où le joueur veut |
| Sens | Vente entre joueurs | Vente uniquement |
| Prix | Libre | Libre |
| Stock | Escrow serveur | Dans le bloc |
| Ventes hors ligne | Oui | Oui |
| Paiement | Crédits (banque) | Crédits (banque) |
| Taxe → Trésor | **5 %** | **2 %** |

---

## 5. Sécurité & anti-abus (exigences dures)

- **Aucune duplication** : un objet en vente est soit en escrow serveur, soit dans le bloc — jamais
  aussi dans l'inventaire du joueur. Toute opération argent + objet doit être **atomique**.
- **Aucune création d'argent hors robinet** : banque, marché, boutiques, /payer ne font que déplacer.
- **Ventes/paiements hors ligne fiables** : l'état vit côté serveur (données sauvegardées), pas dans
  la session du joueur.
- **Confirmation** obligatoire avant un paiement direct (éviter les erreurs de montant).
- **Contrôle d'accès** : seul le proprio remplit/vide son bloc-boutique ; seuls les admins touchent
  le Trésor et les taux.
- **Historique** de toutes les transactions pour audit RP et débogage.

---

## 6. Correspondance technique (NeoForge 1.21.1)

Indications de mise en œuvre (détaillées ensuite dans le plan d'implémentation) :

- **Comptes / Trésor / annonces marché** → `SavedData` (données attachées au monde, persistées).
- **Villageois Banquier / Marchand** → `VillagerProfession` custom + `PoiType` custom lié au bloc de
  métier ; blocs de métier enregistrés dans le POI pour l'acquisition du métier.
- **Menus (banque, marché, carnet)** → `MenuType` + écran client + **paquets réseau** (`CustomPacketPayload`)
  pour les actions (déposer, acheter, payer…), toute la logique **validée côté serveur**.
- **Bloc-boutique** → `Block` + `BlockEntity` (stock + prix + proprio) + menu.
- **Commande `/payer`** → `Commands` (Brigadier), argument joueur + montant.
- **Publication hebdo des commandes + prospérité** → compteur de ticks serveur, déclenché sur un
  cycle de jours de jeu.

Tout enregistrement passe par les `DeferredRegister` NeoForge (blocs, items, block entities,
menus, professions, POI, commandes via events).

---

## 7. Configuration externe (hors mod custom)

Deux réglages du pack, indépendants du mod, mais indispensables à l'équilibre :

- **MoreJS** (à ajouter au pack) + KubeJS : nettoyer les échanges villageois.
  - ❌ Retirer les échanges où le villageois **donne** des émeraudes au joueur.
  - ✅ Garder les échanges où le villageois **vend** un objet **contre** des émeraudes.
  - Vérifier séparément **Easy Villagers** et **Goblin Traders**.
- **Bountiful 8.0.0-beta.2** (déjà présent) : configurer les *bounty pools*, *decrees* et
  récompenses en **émeraudes** (`config/bountiful/`). Contrats en rapport avec ce qu'un village
  demanderait : agriculture, cuisine, mine, bois, construction, composants Create, et **quelques
  contrats de chasse aux monstres**. Recharge via `/bo settings reload`.

---

## 8. Ordre de construction proposé (phases)

Même si tout est codé à la main, on livre **par tranches testables** :

1. **Fondations** : mod qui charge, monnaie/crédits, comptes, `SavedData`, commandes admin de base.
2. **Banque** : Villageois Banquier + table de métier + dépôt/retrait + menu + historique.
3. **Paiements** : carnet bancaire + `/payer` + confirmations.
4. **Boutiques perso** : bloc-boutique + escrow dans le bloc + taxe 2 %.
5. **Marché central** : Villageois Marchand + comptoir + annonces + escrow serveur + taxe 5 %.
6. **Trésor + Besoins/Prospérité** : commandes municipales hebdo + consommation + prospérité.

En parallèle (config pack) : **MoreJS** + **Bountiful** dès la phase 1 pour une économie de base
jouable rapidement.

---

## 9. Questions ouvertes / à trancher plus tard

- Nom et *modid* définitifs du mod (proposition au scaffolding, ex. `citeconomy` / `fdpcfa`).
- Détail exact des besoins hebdo par palier de population et de la courbe de Prospérité.
- Faut-il un **plafond de retrait** ou d'annonces par joueur pour limiter les abus.
- Bootstrapping : au tout début le Trésor est vide (pas encore de taxes) → les joueurs gagnent
  d'abord via mines + Bountiful, puis les commandes municipales démarrent une fois le Trésor
  alimenté. À confirmer que ce démarrage progressif te convient.
- Apparence des blocs (textures, modèles) — cosmétique, après le fonctionnel.
