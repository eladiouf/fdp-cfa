# Économie de la Cité — Document de conception

> Mod custom NeoForge 1.21.1 pour le serveur RP « La Première Cité »
> (modpack *Create Ultimate Selection 2*, MC 1.21.1, NeoForge).
> Mod ID : `citeconomy` · Groupe : `com.citeconomy` · Langue : Java 21 · Build : Gradle + MDK 2.0.142

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

**Circuit 1 — Création de monnaie (le « robinet »)**
Seuls **deux** canaux font entrer des émeraudes **neuves** dans l'économie :
1. les **mines** (minerai d'émeraude) ;
2. les **contrats Bountiful** (payés en émeraudes).

**Circuit 2 — Circulation & redistribution (aucune création)**
Ne créent **aucune** émeraude — ils ne font que déplacer de l'argent existant :
- le **marché central**, les **boutiques perso** et les **paiements directs** (joueur → joueur) ;
- les **commandes municipales**, **payées par le Trésor** (donc par les taxes déjà collectées).
  Le Trésor est une **cagnotte de redistribution**, pas un robinet : s'il est vide, aucune
  commande n'est publiée (§3.6).

> Chaîne complète : mines/Bountiful → joueurs → marché → **taxes** → Trésor → **commandes
> municipales** → joueurs. Auto-équilibrée : une fois le budget de départ épuisé, la ville ne
> peut redistribuer que ce qu'elle a taxé.

> **Budget de départ** : le Trésor démarre avec une somme initiale (configurable par l'admin) pour
> que les commandes municipales puissent tourner **dès le jour 1**. C'est une dotation **unique**,
> pas un robinet récurrent : ensuite le Trésor ne se remplit que par les taxes.

**Les drains**
- Les **besoins des villageois** consomment des produits payés par le Trésor (l'argent ressort).
- Les **échanges villageois** où le joueur **achète** un objet contre des émeraudes retirent de
  la monnaie.
- Les **taxes de marché** transfèrent une part au Trésor (redistribuée ensuite en commandes).

> ⚠️ Sans contrôle des échanges villageois (Easy Villagers / Goblin Traders), les joueurs
> fabriqueraient des émeraudes à volonté et la monnaie s'effondrerait. Voir §7.

---

## 3. Composants du mod

Le mod regroupe **sept** systèmes. Chacun est indépendant et testable séparément.

### 3.1 Banque + Villageois Banquier

#### Resource Locations
- `citeconomy:banker_table` — bloc (Table du Banquier)
- `citeconomy:banker_poi` — type de point d'intérêt (POI)
- `citeconomy:banker` — profession de villageois
- `citeconomy:bank_menu` — type de menu

#### Comptes
- **Compte virtuel par joueur** (solde en crédits), persistant, sauvegardé côté serveur
  dans `EconomyData` (SavedData, fichier `data/citeconomy_data`).
- Stocké dans une `Map<UUID, Integer>`.

#### Villageois Banquier
- **Ne spawn jamais naturellement.** Un villageois devient Banquier uniquement si un joueur
  **fabrique et pose sa table de métier** (bloc `citeconomy:banker_table`).
- Interaction → ouvre le menu de la banque (`BankMenu` + `BankScreen`).
- Handler : `ServerEvents.onEntityInteract()` — intercepte `PlayerInteractEvent.EntityInteract`
  sur tout `Villager` avec la profession `BANKER_PROFESSION`.

#### Table du Banquier (bloc)
- **Comportement** : bloc de métier (job site block). Un villageois le détecte et peut
  acquérir la profession `citeconomy:banker`.
- **POI** : `BankerPOI` lié aux `BlockState` de `BANKER_TABLE`.
- **Exigence technique** : le POI `citeconomy:banker_poi` DOIT être listé dans le tag
  `minecraft:point_of_interest_type/acquirable_job_site.json` pour que les villageois
  puissent l'acquérir. Fichier à créer :
  ```json
  {
    "values": ["citeconomy:banker_poi"]
  }
  ```
  Emplacement : `src/main/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json`
- **Constructible** : craft en forme (3 émeraudes en haut, 3 planches au milieu, 2 planches en bas).

#### Opérations bancaires
- **Dépôt** : émeraudes physiques → crédits (ratio 1:100). Le joueur clique "Déposer"
  dans le menu → `BankTransactionPayload` (C→S) → le serveur cherche les émeraudes
  dans l'inventaire du joueur, les retire, ajoute les crédits → répond `SyncBalancePayload`.
- **Retrait** : crédits → émeraudes physiques (par tranche de 100). Les crédits en dessous
  de 100 restent sur le compte.
- **Solde** : affiché dans l'écran banque + via le `BankbookItem` (use → message en chat).

#### État actuel du code
- Banquier fonctionnel (profession, POI, menu, dépôt/retrait).
- Tag `acquirable_job_site.json` : OK (banker_poi + merchant_poi).

---

### 3.2 Marché central + Villageois Marchand

**✅ IMPLÉMENTÉ** voir `ServerEvents.java`, `MarketCommand.java`, `MarketMenu.java`, `MarketScreen.java`.

#### Resource Locations
- `citeconomy:merchant_counter` — bloc (Comptoir du Marchand)
- `citeconomy:merchant_poi` — type de point d'intérêt
- `citeconomy:merchant` — profession de villageois
- `citeconomy:market_menu` — type de menu

#### Villageois Marchand
- **Ne spawn jamais naturellement.** Nécessite un bloc « Comptoir du Marchand » fabriqué et posé.
- Situé dans le bâtiment du marché (choix des joueurs).
- Interaction → ouvre le menu du marché.

#### Place de marché joueur-à-joueur
- un joueur met un objet en vente au **prix qu'il veut** (prix libre) ;
- l'objet est mis en **dépôt (escrow) côté serveur** — retiré de l'inventaire, gardé en sûreté
  dans une `Map<UUID, List<MarketListing>>` dans `EconomyData` ;
- un autre joueur l'achète, **même si le vendeur est déconnecté** ;
- le paiement se fait en **crédits** (débité de l'acheteur, crédité au vendeur) ;
- **on peut vendre n'importe quoi** (objet non demandé compris) ;
- **taxe de 5 %** prélevée sur chaque vente → **Trésor de la Cité**.

---

### 3.3 Boutiques personnelles (bloc-boutique)

#### Resource Locations
- `citeconomy:personal_shop` — bloc (Boutique Personnelle)
- `citeconomy:personal_shop` — type de block entity
- `citeconomy:personal_shop_menu` — type de menu

#### Fonctionnement
- **Bloc craftable** que chaque joueur pose où il veut (maison, quartier, place…).
- Recette : planches + coffre.
- **Vente uniquement** (l'achat automatique par le bloc est une évolution future).
- Le propriétaire **remplit le bloc** de son stock (27 slots) et fixe un **prix libre**
  (prix unique pour tous les slots, stocké dans le `CompoundTag` de la block entity).
- Un client interagit → ouvre le menu → clique sur un slot → envoie `ShopBuyPayload` (C→S)
  → le serveur vérifie le solde, prélève le prix, applique la **taxe de 2 %** (→ Trésor),
  crédite le vendeur, retire l'item du stock et le donne à l'acheteur.
- Le vendeur est crédité **même hors ligne** (le bloc garde le stock côté serveur).
- Seul le propriétaire peut remplir/reprendre le stock (vérification `ownerId`).

#### Block Entity (`PersonalShopBlockEntity`)
- Stocke : `ownerId` (UUID), `ownerName` (String), `price` (int), `inventory` (ItemStackHandler, 27 slots).
- Persistance : `saveAdditional()` / `loadAdditional()` via NBT.
- **BUG CONNU** : `setOwner()` n'est appelé nulle part actuellement.
  **Correction nécessaire** : dans `PersonalShopBlock`, override `setPlacedBy()` pour appeler
  `blockEntity.setOwner(player.getUUID(), player.getName().getString())` au placement du bloc.
  Sans cette correction, `ownerId = null` → impossible de vérifier le propriétaire → n'importe
  qui peut modifier le stock, et les achats échouent.

---

### 3.4 Paiements directs entre joueurs

#### Resource Locations
- `citeconomy:bankbook` — item (Carnet Bancaire)

#### Méthode 1 : Carnet Bancaire (objet)
- **État actuel** : `BankbookItem.use()` envoie simplement le solde en message chat.
- **Comportement attendu (non implémenté)** : doit ouvrir un menu avec :
  1. champ de saisie du destinataire (auto-complétion des joueurs connectés)
  2. champ de saisie du montant
  3. bouton de confirmation avant envoi
- L'implémentation du menu carnet est à faire.

#### Méthode 2 : Commande `/payer`
- **Implémentée** dans `EconomyCommand.register()`.
- Syntaxe : `/payer <joueur> <montant>`
- Validations : pas de paiement à soi-même, fonds suffisants.
- Fonctionne **même si le destinataire est déconnecté** (économise sur le UUID).
- **Sans taxe** (transfert privé, pas une vente).

---

### 3.5 Trésor de la Cité

#### Fonctionnement
- **Compte spécial** dans `EconomyData` (pas un joueur) qui reçoit **toutes les taxes** :
  - 5 % des ventes du marché central (non implémenté)
  - 2 % des ventes des boutiques personnelles (implémenté dans `ShopBuyPayload`)
- **Démarre avec un budget de départ** (dotation unique). Valeur par défaut proposée :
  `5000 crédits (= 50 émeraudes)`.
- Sert à **financer les commandes municipales** (§3.6).
- Si le Trésor est vide → plus de commandes publiées.
- **Consultable** par tous ; **modifiable** seulement via les commandes admin `/citeconomy admin treasury`.
- Commandes : `/citeconomy treasury set <montant>`, `/citeconomy treasury add <montant>`.

#### Commandes admin
- `/citeconomy admin set <joueur> <montant>` — fixer le solde d'un joueur
- `/citeconomy admin add <joueur> <montant>` — ajouter des crédits
- `/citeconomy admin remove <joueur> <montant>` — retirer des crédits
- `/citeconomy treasury set <montant>` — fixer le Trésor
- `/citeconomy treasury add <montant>` — ajouter au Trésor
- `/citeconomy cycle` — exécuter le cycle économique manuellement (debug/forced)

---

### 3.6 Besoins de la population + Prospérité

**✅ IMPLÉMENTÉ** voir `WeeklyQuest.java`, `NeedsCommand.java`, `NeedsMenu.java`, `NeedsScreen.java`, `QuestCompletePayload.java`.

Le système qui donne une « vie » économique à la ville sans MineColonies.

#### Cycle
1. **Chaque semaine (de jeu)**, le Marchand publie automatiquement des **commandes municipales**
   selon le **nombre de villageois** dans un rayon de la ville.
2. Les joueurs **remplissent** ces commandes en livrant les items demandés.
3. Le **Trésor les paie en crédits** (argent déjà taxé, pas d'émeraudes neuves créées).
4. Les produits livrés sont **consommés** par la population (drain).
5. Si les besoins sont satisfaits → la **Prospérité de la Cité** monte.
6. Si le **Trésor est vide** → **plus de commandes** publiées.

#### Métriques à définir
- Cycle de temps : 1 semaine MC = 7 jours MC = 168 000 ticks (à 20 ticks/s).
- Les commandes sont stockées dans `EconomyData` sous forme de `List<MunicipalOrder>`.
- Les besoins sont catégorisés par `ItemTag` NeoForge :
  - `citeconomy:needs/food` — toute nourriture comestible
  - `citeconomy:needs/wool` — laine + produits dérivés
  - `citeconomy:needs/tools` — outils en pierre/fer
  - `citeconomy:needs/torches` — torches + lanternes
  - `citeconomy:needs/wood` — bois + planches
  - `citeconomy:needs/stone` — pierre taillée, briques
  - `citeconomy:needs/comfort` — lits, verrières, décorations

#### Palier de Prospérité (à valider)
| Score | Nom | Effets |
|-------|-----|--------|
| 0-50 | Hameau | Commandes de base (nourriture, torches) |
| 50-200 | Village | + commandes outils, laine. Taxe boutiques 1% |
| 200-500 | Ville | + commandes bois, pierre. Taxe marché 3% |
| 500+ | Cité | Toutes commandes. Nouvelles capacités RP |

> Les villageois ne construisent rien eux-mêmes : ils créent une **demande économique permanente**.
> Ce sont toujours les joueurs qui bâtissent et font évoluer la ville.

---

### 3.7 Entreprises

Système permettant à des groupes de joueurs de créer une entreprise commune.
Une entreprise a un compte bancaire partagé, des employés, et un salaire configurable.

#### Resource Locations
- Commande : `/entreprise`
- Classe : `Company` stockée dans `EconomyData.companies` (`Map<UUID, Company>`)

#### Commandes
| Commande | Qui peut | Description |
|----------|----------|-------------|
| `/entreprise creer <nom>` | Joueur sans entreprise | Crée une entreprise. Le créateur devient propriétaire + 1er employé |
| `/entreprise inviter <joueur>` | Propriétaire | Envoie une invitation |
| `/entreprise accepter` | Joueur invité | Accepte l'invitation en attente |
| `/entreprise deposer <montant>` | Tout employé | Dépose des crédits personnels → compte entreprise |
| `/entreprise retirer <montant>` | Propriétaire uniquement | Retire des crédits → compte personnel |
| `/entreprise salaire <montant>` | Propriétaire | Fixe le salaire par cycle (0 = pas de salaire) |
| `/entreprise quitter` | Employé (sauf proprio) | Quitte l'entreprise |
| `/entreprise dissoudre` | Propriétaire | Dissout l'entreprise, distribue le solde à tous les employés |
| `/entreprise infos` | Tout employé | Affiche les infos (nom, proprio, solde, salaire, employés) |

#### Stockage (`Company`)
- `UUID id`, `String name`, `UUID owner`, `int balance`, `int salary` (défaut: 100), `Set<UUID> employees`
- Persistance NBT via `Company.save()` / `Company.load()`.

#### Cycle économique automatique
- Déclenché toutes les 24 000 ticks (1 jour Minecraft) via `ServerTickEvent.Post`.
- Pour chaque entreprise :
  1. **Taxe** : 10 % du solde de l'entreprise → Trésor (incite à ne pas thésauriser)
  2. **Salaires** : `salaire` crédits à chaque employé depuis le compte entreprise
     (déplacement, pas création de monnaie)
- Si l'entreprise n'a pas assez pour payer tous les salaires, seuls les premiers sont payés.
- Les joueurs connectés reçoivent un message. Le cycle tourne même si personne n'est connecté.

#### Équilibre économique
- La société ne crée **pas** d'argent : les salaires sont prélevés de son compte, les dépôts viennent
  des comptes personnels des employés.
- La taxe de 10% alimente le Trésor et redistribue à la ville via les commandes municipales.
- Utile pour : financer des achats de groupe, récompenser les employés, spécialisation RP.

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

## 5. Paquets réseau (CustomPacketPayload)

| Payload | Direction | Contenu | Usage |
|---------|-----------|---------|-------|
| `BankTransactionPayload` | C → S | `boolean isDeposit`, `int amount` | Déposer/retirer des émeraudes |
| `SyncBalancePayload` | S → C | `int balance` | Sync solde après opération |
| `ShopBuyPayload` | C → S | `BlockPos pos`, `int slot` | Acheter un item en boutique |
| `SetShopPricePayload` | C → S | `BlockPos pos`, `int price` | Définir le prix d'une boutique |
| `BankbookPayPayload` | C → S | `UUID target`, `int amount` | Paiement via carnet bancaire |
| `MarketBuyPayload` | C → S | `UUID listingId` | Acheter une annonce du marché |
| `MarketListingsPayload` | S → C | `List<Entry>` | Sync des annonces du marché |
| `NeedsDataPayload` | S → C | `List<WeeklyQuest>` | Sync des besoins au client |
| `QuestCompletePayload` | C → S | `int questId` | Réclamer la récompense d'un besoin |

Registrar : `ModNetworking.register()` — version `"1"`.

## 5b. Commande de consultation

| Commande | Perf | Description |
|----------|------|-------------|
| `/solde` | Joueur | Affiche le solde + rappel de `/historique` |
| `/historique` | Joueur | Affiche les 10 dernières transactions du joueur |
| `/citeconomy admin history <joueur>` | OP | Affiche les 20 dernières transactions d'un joueur |

### TransactionLog

Enregistré dans `EconomyData.transactionHistory` (`Map<UUID, List<TransactionLog>>`), max 50 par joueur.
Chaque log : `timestamp`, `type` (CREDIT/DEBIT/SALARY), `amount`, `balanceAfter`, `description`, `secondParty`.
Persistance NBT via `TransactionLog.save()` / `TransactionLog.load()`. Toute opération monétaire
(banque, paiement, achat, salaire, admin) enregistre automatiquement un log.

---

## 6. Sécurité & anti-abus (exigences dures)

- **Aucune duplication** : un objet en vente est soit en escrow serveur, soit dans le bloc — jamais
  aussi dans l'inventaire du joueur. Toute opération argent + objet doit être **atomique**.
  Vérifié dans `ShopBuyPayload.handleData()` : extraction atomique via `extractItem()`.
- **Aucune création d'argent hors robinet** : banque, marché, boutiques, `/payer` ne font que déplacer.
  **Exception** : le cycle économique (`/citeconomy cycle`) ne crée pas d'argent non plus — il
  redistribue depuis les comptes entreprises.
- **Ventes/paiements hors ligne fiables** : l'état vit côté serveur (`EconomyData`, `SavedData`,
  `PersonalShopBlockEntity`), pas dans la session du joueur.
- **Confirmation** : obligatoire avant un paiement direct (le carnet bancaire, quand il sera
  implémenté, aura un écran de confirmation ; `/payer` est immédiat).
- **Contrôle d'accès** : seul le proprio remplit/vide son bloc-boutique (vérification `ownerId`) ;
  seuls les admins touchent le Trésor (permission OP level 2).
- **Historique** : **✅ IMPLÉMENTÉ** via `TransactionLog.java` + commandes `/historique` et `/citeconomy admin history`.
  Devra être ajouté dans `EconomyData` sous forme de `List<TransactionLog>`.

---

## 7. Correspondance technique (NeoForge 1.21.1)

### Registres (DeferredRegister)

| Registre | Classe | Éléments |
|----------|--------|----------|
| BLOCKS | `ModBlocks` | `banker_table`, `personal_shop` |
| ITEMS | `ModItems` | `banker_table` (BlockItem), `bankbook`, `personal_shop` (BlockItem) |
| BLOCK_ENTITIES | `ModBlockEntities` | `personal_shop` |
| MENUS | `ModMenus` | `bank_menu`, `personal_shop_menu` |
| CREATIVE_MODE_TABS | `ModCreativeTabs` | `citeconomy_tab` |
| POI_TYPES | `ModVillagers` | `banker_poi` |
| VILLAGER_PROFESSIONS | `ModVillagers` | `banker` |

### Données sauvegardées (`SavedData`)
- `EconomyData` — attaché au monde de l'Overworld.
- Stocke : `Map<UUID, Integer> balances`, `Map<UUID, Company> companies`, `int treasuryBalance`.
- Factory : `EconomyData.factory()` → load/save via NBT.
- Toutes les méthodes de modification appellent `setDirty()`.

### Structures

#### Structure des dossiers
```
src/main/
├── java/com/citeconomy/
│   ├── CiteconomyMod.java
│   ├── block/
│   │   ├── PersonalShopBlock.java
│   │   └── entity/
│   │       └── PersonalShopBlockEntity.java
│   ├── client/
│   │   ├── ClientEvents.java
│   │   ├── ClientState.java
│   │   └── gui/
│   │       ├── BankScreen.java
│   │       └── PersonalShopScreen.java
│   ├── command/
│   │   ├── CompanyCommand.java
│   │   └── EconomyCommand.java
│   ├── data/
│   │   ├── Company.java
│   │   └── EconomyData.java
│   ├── event/
│   │   └── ServerEvents.java
│   ├── item/
│   │   └── BankbookItem.java
│   ├── menu/
│   │   ├── BankMenu.java
│   │   └── PersonalShopMenu.java
│   ├── network/
│   │   ├── BankTransactionPayload.java
│   │   ├── ModNetworking.java
│   │   ├── ShopBuyPayload.java
│   │   └── SyncBalancePayload.java
│   └── registry/
│       ├── ModBlockEntities.java
│       ├── ModBlocks.java
│       ├── ModCreativeTabs.java
│       ├── ModItems.java
│       ├── ModMenus.java
│       └── ModVillagers.java
├── resources/
│   ├── assets/citeconomy/
│   │   ├── blockstates/ (banker_table, personal_shop)
│   │   ├── lang/ (en_us, fr_fr)
│   │   ├── models/block/ (banker_table, personal_shop)
│   │   ├── models/item/ (bankbook, banker_table, personal_shop)
│   │   └── textures/block/ (banker_table, personal_shop)
│   │       textures/item/ (bankbook)
│   └── data/citeconomy/recipe/ (bankbook, banker_table, personal_shop)
└── templates/
    └── META-INF/neoforge.mods.toml
```

---

## 8. Configuration externe (hors mod custom)

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

## 9. État d'avancement & bugs connus

### Ce qui est implémenté
- [x] Phase 1 — Fondations (mod, registries, SavedData, commandes admin)
- [x] Phase 2 — Banque (Banker, dépôt/retrait, menu basique, BankbookItem basique)
- [x] Phase 3 — Carnet bancaire (menu, destinataire, montant, confirmation) — *via Antigravity (?)*
- [x] Phase 4 — Boutiques perso (bloc, block entity, menu, achat via clic)
- [x] Phase 5 — Marché central (Marchand, Comptoir, escrow, `/marche vendre`, menu achat)
- [x] Paiements directs (`/payer`)
- [x] Système Entreprise amélioré (Company, 9 commandes, cycle éco automatique)
- [x] Textures, modèles, lang, recettes

### Ce qui manque
- [ ] Rien pour l'instant (toutes les phases sont implémentées)

### Bugs — tous corrigés

| # | Bug | Fichier | Statut |
|---|-----|---------|--------|
| 1 | Tag POI `acquirable_job_site.json` manquant → le Banquier ne peut pas être acquis | `ModVillagers` | ✅ Corrigé (présent dans `/data/minecraft/tags/`) |
| 2 | `setOwner()` jamais appelé → `ownerId = null` → boutique cassée | `PersonalShopBlock.java` | ✅ Corrigé |
| 3 | BankScreen limité à 1 émeraude — pas de champ montant | `BankScreen.java` | ✅ Corrigé |
| 4 | BankTransactionPayload : retrait sans vérif de place inventaire | `BankTransactionPayload.java` | ✅ Corrigé |
| 5 | Pas d'historique des transactions | `EconomyData.java` | ✅ Corrigé |

---

## 10. Configuration (valeurs par défaut proposées)

Ces valeurs seront placées dans un fichier de config (`.toml`) via NeoForge :

| Propriété | Valeur par défaut | Description |
|-----------|------------------|-------------|
| `treasury.startingBudget` | `5000` | Budget de départ du Trésor (en crédits) |
| `tax.market` | `5` | Taxe du marché central (%) |
| `tax.shop` | `2` | Taxe des boutiques personnelles (%) |
| `tax.company` | `10` | Taxe des entreprises (%) |
| `salary.company` | `100` | Salaire par employé par cycle (crédits) |
| `cycle.interval` | `24000` | Ticks entre chaque cycle éco (1 jour MC) |

---

## 11. Décisions prises & questions restantes

### Décidé
- **`modid` = `citeconomy`** (nom technique du mod).
- **Budget de départ du Trésor** activé → commandes municipales dès le jour 1 (dotation unique
  configurable, valeur proposée : 5000 crédits).
- **Aucun plafond** de retrait, d'annonces ou de boutiques par joueur — on reste simple.
- **Entreprises** : présent dans le code mais pas dans le design original — à valider.

### Questions en suspens
- Système d'entreprise : on garde ou on supprime ?
- Détail exact des besoins hebdo par palier de population et de la courbe de Prospérité
  (voir §3.6 pour la proposition).
- Apparence des blocs (textures, modèles) — les textures actuelles sont provisoires
  (bloc carré `cube_all` avec texture custom).
