# DKT Workflow - aplikace pro plně automatické zpracování skenů kramářských tisků do NDK balíčků
Aplikace pro rychlé vytvoření NDK balíčků z naskenovaných kramářských tisků. Výchozí předpoklady: jedná se o nečíslované stránky, do systému vstupují už ořezané.

Nástroj pro extrakci dat a jejich zpřístupnění - cílem nástroje je plně zautomatizovat proces tvorby metadatových balíčků odpovídajících standardu Národní digitální knihovny při digitalizaci kramářských tisků jako zástupce kategorie dokumentů, které se vyznačují velmi nízkým počtem nečíslovaných stran na svazek a (4, 8 apod.). Aplikace má na vstupu sekvenci snímků mnoha tisků v jedné dávce, včetně snímků ochranných obálek s jejich čárovými kódy. Vzhledem ke způsobu snímání digitálním fotoaparátem jsou snímky stran jednotlivých tisků uspořádány ve stejném pořadí, v jakém byly nasnímány: nejprve všechny pravé a pak všechny levé stránky. Aplikace projde všechny snímky, automaticky identifikuje, na kterých z nich jsou čárové kódy a sekvenci rozdělí na snímky patřící k jednotlivým tiskům, správně seřadí stránky, na základě čárového kódu doplní bibliografická metadata stažením z katalogu přes protokol Z39.50, zkonvertuje je do MODSu, vygeneruje strukturální metadata v METS, zajistí vytvoření OCR (text + ALTO) prostřednictvím PERO OCR, zkonvertuje skeny do jpeg2000 (master a user copy) a vytvoří ze všech informací balíček v souladu se standardy NDK, připravený k importu do digitální knihovny Kramerius a do systému pro dlouhodobou archivaci digitalizovaných dokumentů. Takto realizovaný proces znamená výraznou personální úsporu, protože eliminuje veškeré ruční manipulace s jednotlivými skeny od jejich naskenování a ořezu. Tím dochází jednak k významné úspoře času (není potřeba pracovník na editaci strukturálních metadat, která je pracná a náročná na pozornost) a zároveň nemůže dojít k chybám z nepozornosti, což při zpracování desítek tisíc drobných tisků může snadno nastávat.  

---

## Hlavní komponenty:

1. dkt-workflow.jar (Java) – centrální orchestrátor
* obsahuje logiku workflow včetně pravidel pojmenování/řazení obrazů a rozdělování na jednotlivé svazky/tisky
* připravuje vstupy a zpracovává výstupy externích nástrojů, manipuluje s meziprodukty (obrazy, metadata, OCR)
* generuje finální NDK balíčky
* je spustitelný přes CLI; CLI umí i „pre-flight“ kontrolu dostupnosti nástrojů (ImageMagick, Kakadu apod.)

2. Detektor čárového kódu (Python)
* optická detekce čárového kódu z obrázku (Pyzbar)

3. MarcXML provider (Python + yaz-client, Z39.50)
* získání bibliografického záznamu z Alephu přes protokol Z39.50
* Z39.50 klientská část je řešená toolkitem YAZ (yaz-client)

4. Konverze metadat (XSLT + Java doplnění)
* MARCXML → MODS (základ přes XSLT, následné obohacení v Javě)
* MODS → Dublin Core (XSLT)
* MODS konverze a mapování jsou realizované přes XSLT crosswalky (LoC), které lze upravovat pro lokální potřeby

5. Konverze obrazů (CLI nástroje + skripty)
* TIF → PNG přes ImageMagick (bash) – PNG je potřeba pro PERO
* TIF → JP2 přes Kakadu (uživatelská + archivní kopie pro NDK)
* Kakadu lze v případě potřeby nahradit jiným JPEG2000 toolkitem

6. OCR provider – PERO (HTTP API, asynchronní režim)
* odeslání stránky na OCR a periodické dotazování na stav až do dokončení, poté stažení textu a ALTO - využívá API systému PERO

---

## Příprava obrázků

První tisk: nejprve se nafotí čárový kód na bílém podkladu, pak všechny pravé strany konkrétního tisku, potom všechny levé. A následuje další čárový kód a další tisk. Tímto dohodnutým postupem se vytvoří balík obrázků obsahující několik kramářských tisků. Nad takto vyrobeným balíkem obrázků (několika tisků dohromady) se pak spouští dkt-workflow.

---

## Konverze nástrojem dkt-workflow

Jako vstup pro jeden tisk se vyberou obrázky po skenu čárového kód a před dalším čárovým kódem (pro následující tisk). Kontroluje se, zda sedí počty stran - celkový počet stran a pravé vůči levým.  
Podle čárových kódů se z Alephu přes binární protokol Z39.50 získává záznam v Marc21, který je zdrojem metadat pro výsledný balíček (MODS, DC). Obrázky se konvertují do dalších formátů (PNG, J2K). Z obrázků se dále přes OCR PERO generují textové a strukturované přepisy (ALTO). Výsledkem je pro každou vstupní dávku obrázků několik NDK balíčků určených pro import do Krameria a případnou archivaci. Tyto balíčky jsou uloženy do určeného adresáře - podadresář pro konkrétní vstupní adresář a konkrétní běh dkt-worflow.

---

## Aplikace dkt-workflow.jar

Hlavní komponentou systému je aplikace dkt-workflow (workflow pro digitalizaci kramářských tisků) je napsaná v javě. Obsahuje:
* logiku samotného workflow. Včetně pravidel pro pojmenování a uspořádání obrázků a rozdělování do jednotlivých svazků
* rutiny pro volání externích služeb a programů přes HTTP a CLI rozhraní, přímo, nebo přes další skripty (python/bash/…)
* kód pro přípravu vstupů a zpracování výstupů externích nástrojů
* manipulaci s meziprodukty (soubory obrázků, metadat, ocr dat)
* generování finálních NDK balíčků

Komponenta dkt-workflow tedy koordinuje celý proces, část zpracování dat vykonává a část deleguje na externí nástroje a orchestruje jejich postupná volání. Nástroje se volají přes CLI, případně s pomocí jednoduchých skriptů (bash). CLI dkt-workflow umožňuje před samotným spuštěním konverze nejprve zkontrolovat dostupnost jednotlivých externích nástrojů (např. zda je nainstalován imageMagick, nebo je dostupný Kakadu ve vhodné verzi).

---

## Dedikace

Software vznikl na základě institucionální podpory dlouhodobého koncepčního rozvoje výzkumné organizace poskytované Ministerstvem kultury.											
