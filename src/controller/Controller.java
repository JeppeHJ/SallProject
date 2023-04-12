package controller;

import application.*;
import storage.Storage;

import java.time.LocalDate;
import java.util.*;

/**
 * Controller-klassen
 * Den bruger Singleton designmønsteret for at sikre, at kun én instans
 * af Controller oprettes i løbet af programmets levetid.
 */
public class Controller {
    private static Controller controller;
    private Storage storage;

    private Controller() {
        storage = Storage.getStorage();
    }

    public static Controller getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    /**
     * -------------- LAGER METODER --------------
     **/

    // Opretter et nyt lager og tilføjer det til storage
    public Lager opretLager(String lokation, int antalPladser) {
        Lager lager = new Lager(lokation, antalPladser);
        storage.addLager(lager);
        return lager;
    }

    // Henter alle lagre fra storage
    public ArrayList<Lager> getAlleLagre() {
        return storage.getLagre();
    }

    // Henter en HashMap med fade for et givet lagerID
    public HashMap<Integer, Fad> getFadeIHashMap(int lagerId) {
        Lager lager = storage.getLagerById(lagerId);
        if (lager != null) {
            return lager.getFadeHashMap();
        }
        return new HashMap<>();
    }

    // Henter et lager med et bestemt ID fra storage
    public Lager getLagerById(int id) {
        return storage.getLagerById(id);
    }

    // Returnerer det samlede antal lagre
    public int totalAntalLager() {
        return storage.getLagre().size();
    }

    /**
     * -------------- FAD METODER --------------
     **/

    // Opretter et nyt Fad objekt og tilføjer det til storage og lager
    public Fad opretFad(String fadType, double fadStr, Lager lager, int plads) {
        Fad fad = new Fad(fadType, fadStr);
        // Tilføj fad til lager og storage
        storage.getLagerById(lager.getId()).addFad(fad, plads);
        storage.addFad(fad, plads);
        return fad;
    }

    public ArrayList<Fad> getAlleFad() {
        return storage.getFade();
    }

    public ArrayList<Fad> getAlleFadDerKanOmhældesTil(Fad inputFad) {
        ArrayList<Fad> fade = new ArrayList<>();
        for (Fad fad : storage.getFade()) {
            if (fad != inputFad) {
                if (fad.getFadStr() - fad.getFadfyldning() != 0) {
                    fade.add(fad);
                }
            }

        }
        return fade;
    }

    public ArrayList<Fad> getAlleFadDerKanOmhældesFra() {
        ArrayList<Fad> fade = new ArrayList<>();
        for (Fad fad : storage.getFade()) {
            if (fad.getFadStr() - fad.getFadfyldning() != fad.getFadStr()) {
                fade.add(fad);
            }
        }
        return fade;
    }

    // Returnerer en liste af fade med færdiglagret væske
    public ArrayList<Fad> getFadeMedFærdigVæske() {
        ArrayList<Fad> fade = new ArrayList<>();
        for (Fad fad : storage.getFade()) {
            if (fad.erFaerdigLagret()) {
                fade.add(fad);
            }
        }
        return fade;
    }

    public LagretVæske opretNyLagretVæskeOmhældning(Fad kilde, Fad destination, double mængde) {

        ArrayList<Distillat> kildeDestillatListe = kilde.getLagretVæsker().get(0).getDistillater();
        ArrayList<Distillat> destinationDestillatListe = null;
        LagretVæske destLv = null;
        if (!(destination.getLagretVæsker().isEmpty())) {
            destinationDestillatListe = destination.getLagretVæsker().get(0).getDistillater();
            destLv = destination.getLagretVæsker().get(0);
        }
        // Cojoin the lists into a new
        LagretVæske kildeLv = kilde.getLagretVæsker().get(0);
        LagretVæske nyeVæske = controller.opretLagretVæske(mængde + destination.getFadfyldning(), LocalDate.now(), null, kildeDestillatListe, destinationDestillatListe, kildeLv, destLv);

        kilde.omhældning(kilde, destination, mængde, nyeVæske);

        // Handle væsken i kilde-fad
        System.out.println(kilde.getFadfyldning());
        System.out.println(kilde.getFadStr());
        if (kilde.getFadfyldning() == 0) {
            controller.tømtFad(kilde, nyeVæske);
        }


        System.out.println();
        return nyeVæske;
    }

    private void tømtFad(Fad fad, LagretVæske nyeVæske) {
        // Fad skal have et FadsLagretVæskeHistorik objekt med lagretvæske, påfyldningsdato, final omhældningsdato
        fad.editHistoryWhenBarrelEmpty(fad.getLagretVæsker().get(0), LocalDate.now());

        // Kilde Fad er nu tomt i systemet
        fad.removeLagretVæsker(fad.getLagretVæsker().get(0));




    }

    // Returnerer det samlede antal fade i alle lagre
    public int totalAntalFad() {
        int total = 0;
        for (Lager lager : storage.getLagre()) {
            total += lager.amountOfFade();
        }
        return total;
    }

    // Henter en liste af fade, der indeholder en bestemt LagretVæske
    public List<Fad> getBarrelsContainingLagretVaeske(LagretVæske lagretVaeske) {
        List<Fad> barrels = new ArrayList<>();
        for (Fad fad : storage.getFade()) {
            if (fad.getLagretVæsker().contains(lagretVaeske))
                barrels.add(fad);
        }
        return barrels;
    }

    // Metode til at fylde et specifikt Fad op med en given mængde af lagret væske fra et distillat
    public LagretVæske fyldPåSpecifiktFad(double liter, LocalDate påfyldningsDato, Fad fad, Distillat distillat) {
        // Validerer input for liter, fad og distillat
        validateFyldPåSpecifiktFadInput(liter, fad, distillat);

        // Opretter et nyt LagretVæske objekt med den ønskede mængde og dato for påfyldning
        LagretVæske valgtLagretVæske = opretLagretVæske(liter, påfyldningsDato, distillat, null, null, null, null);

        // Finder det ønskede Fad og Distillat i storage
        Fad valgtFad = storage.getFadById(fad.getId());
        Distillat valgtDistillat = storage.getDistillatById(distillat.getId());

        // Påføres det valgte LagretVæske objekt til det valgte Fad
        valgtFad.påfyldning(valgtLagretVæske, påfyldningsDato);

        // Opdaterer mængden af distillat efter påfyldning
        valgtDistillat.setLiter(valgtDistillat.getLiter() - liter);

        // Returnerer det opdaterede LagretVæske objekt
        return valgtLagretVæske;
    }

    private void validateFyldPåSpecifiktFadInput(double liter, Fad fad, Distillat distillat) {
        if (liter <= 0) {
            throw new IllegalArgumentException("Litermængden kan ikke være 0 eller under");
        }
        if (storage.getFadById(fad.getId()) == null) {
            throw new IllegalArgumentException("Fad kunne ikke findes i storage");
        }
        if (storage.getDistillatById(distillat.getId()) == null) {
            throw new IllegalArgumentException("Distillat kunne ikke findes i storage");
        }
        if (liter > (fad.getFadStr() - fad.getFadfyldning())) {
            throw new IllegalArgumentException("Du kan ikke fylde så meget på fadet.");
        }
    }

    // Fylder flere fade op med en given mængde lagret væske fra et distillat
    public void fyldPåFlereFade(double liter, LocalDate påfyldningsDato, ArrayList<Fad> fade, Distillat distillat) {
        // Validerer input
        validateFyldPåFlereFadeInput(liter, distillat);

        // Finder det ønskede Distillat
        Distillat valgtDistillat = storage.getDistillatById(distillat.getId());

        // Initialiserer variabelen for mængden af væske, der skal fyldes på
        double literTilbage = liter;

        // Loop igennem listen af fade
        for (Fad fad : fade) {
            // Tjekker om der er væske tilbage at fylde på
            if (literTilbage > 0) {
                // Beregner ledige kapacitet og mængden af væske, der skal fyldes på dette fad
                double fadCapacity = fad.getFadStr() - fad.getFadfyldning();
                double amountToFill = Math.min(fadCapacity, literTilbage);

                // Fylder væske på dette fad, hvis nødvendigt
                if (amountToFill > 0) {
                    // Opretter og tilføjer LagretVæske til fad og historik
                    LagretVæske lV = opretLagretVæske(amountToFill, påfyldningsDato, valgtDistillat, null, null, null, null);
                    fad.addLagretVæsker(lV);
                    lV.addFadTilHistorik(fad, påfyldningsDato, null);

                    // Opdaterer mængden af væske, der er tilbage at fylde på
                    literTilbage -= amountToFill;
                }
            }
        }
    }

    // Validerer input for fyldPåFlereFade metoden
    private void validateFyldPåFlereFadeInput(double liter, Distillat distillat) {
        // Tjekker om litermængden er positiv
        if (liter <= 0) {
            throw new IllegalArgumentException("Litermængden kan ikke være 0 eller under");
        }
        // Tjekker om distillatet findes i storage
        if (storage.getDistillatById(distillat.getId()) == null) {
            throw new IllegalArgumentException("Distillat kunne ikke findes i storage");
        }
    }

    // Opretter og tilføjer en ny LagretVæske til storage
    public LagretVæske opretLagretVæske(double liter, LocalDate påfyldningsDato, Distillat distillat, ArrayList<Distillat> kildeDestillatListe, ArrayList<Distillat> destinationDestillatListe, LagretVæske kildeLagretVæske, LagretVæske destinationLagretVæske) {
        // Validerer input
        //validateOpretLagretVæskeInput(liter, distillat);

        // hvis den lagrede væske oprettes fra et destillat basically
        if (kildeDestillatListe == null && destinationDestillatListe == null) {
            // Opretter ny LagretVæske
            LagretVæske lagretVæske = new LagretVæske(liter, påfyldningsDato);
            // Tilføjer distillat til LagretVæske
            lagretVæske.addDistillat(storage.getDistillatById(distillat.getId()));
            // Opdaterer distillatets liter
            distillat.subtractFilledLiters(liter);
            // Tilføjer LagretVæske til storage
            storage.addLagretVæske(lagretVæske);
            return lagretVæske;
        } else {
            // Opretter det nye LagretVæske objekt
            // tilføjer de kombinerede distillater til den nye LagretVæskes distillatliste
            LagretVæske lagretVæske = new LagretVæske(liter, påfyldningsDato);
            ArrayList<Distillat> nyListeAfDistillater = joinDistillatLists(kildeDestillatListe, destinationDestillatListe);
            ArrayList<LagretVæskesFadHistorik> nyListeAfLagretVæskesFadHistorik = kildeLagretVæske.getFadehistorik();
            if (destinationLagretVæske != null) {
                nyListeAfLagretVæskesFadHistorik = joinFadHistory(kildeLagretVæske.getFadehistorik(), destinationLagretVæske.getFadehistorik());
                // Co-join the lists of LagretVæskesFadHistorik object stored in kilde and dest
            }
            lagretVæske.addFadHistorikker(nyListeAfLagretVæskesFadHistorik);
            lagretVæske.editHistoryWhenOmhældning(kildeLagretVæske, LocalDate.now());
            lagretVæske.addDestillater(nyListeAfDistillater);
            storage.addLagretVæske(lagretVæske);
            return lagretVæske;

        }


    }

    private ArrayList<Distillat> joinDistillatLists(ArrayList<Distillat> kilde, ArrayList<Distillat> destination) {
        System.out.println("Kilde: " + kilde);
        System.out.println("Destination: " + destination);
        ArrayList<Distillat> combinedList = new ArrayList<>(kilde);

        if (destination != null) {
            // Add all elements from both lists
            combinedList.addAll(kilde);
            combinedList.addAll(destination);

            // Sort the combined list by the getDatoForDone method
            Collections.sort(combinedList, (d1, d2) -> {
                LocalDate date1 = d1.getDatoForDone();
                LocalDate date2 = d2.getDatoForDone();
                return date1.compareTo(date2);
            });
            System.out.println("Condition 1: " + combinedList);
            return combinedList;

        }
        System.out.println("Condition 2: " + combinedList);
        return combinedList;
    }

    private ArrayList<LagretVæskesFadHistorik> joinFadHistory(ArrayList<LagretVæskesFadHistorik> kilde, ArrayList<LagretVæskesFadHistorik> destination) {
        System.out.println("Kilde: " + kilde);
        System.out.println("Destination: " + destination);
        ArrayList<LagretVæskesFadHistorik> combinedList = new ArrayList<>(kilde);

        if (destination != null) {
            // Add all elements from both lists
            combinedList.addAll(kilde);
            combinedList.addAll(destination);

            // Sort the combined list by the getDatoForDone method
            Collections.sort(combinedList, (d1, d2) -> {
                LocalDate date1 = d1.getFraDato();
                LocalDate date2 = d2.getFraDato();
                return date1.compareTo(date2);
            });
            System.out.println("Condition 1: " + combinedList);
            return combinedList;

        }
        System.out.println("Condition 2: " + combinedList);
        return combinedList;
    }


    // Validerer input for opretLagretVæske metoden
    private void validateOpretLagretVæskeInput(double liter, Distillat distillat) {
        if (liter <= 0) {
            throw new IllegalArgumentException("Litermængden kan ikke være 0 eller under");
        }
        if (storage.getDistillatById(distillat.getId()) == null) {
            throw new IllegalArgumentException("Distillat kunne ikke findes i storage");
        }
    }

    // Henter færdige LagretVæsker baseret på deres alder
    public ArrayList<LagretVæske> getFaerdigLagretVaeske() {
        ArrayList<LagretVæske> færdigeVæsker = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        // Gennemgår alle lagrede væsker
        for (LagretVæske væske : storage.getLagretVæsker()) {
            LocalDate påfyldningsDato = væske.getPåfyldningsDato();
            // Tjekker om væsken har lagret i mindst 3 år
            if (påfyldningsDato.plusYears(3).isBefore(currentDate) || påfyldningsDato.plusYears(3).isEqual(currentDate)) {
                færdigeVæsker.add(væske);
            }
        }
        return færdigeVæsker;
    }

    // Henter fadehistorik for en LagretVæske
    public ArrayList<LagretVæskesFadHistorik> getFadehistorik(LagretVæske lagretVæske) {
        // Validerer input
        validateGetFadehistorikInput(lagretVæske);
        // Henter fadehistorik for LagretVæske
        return storage.getLagretVæskeById(lagretVæske.getId()).getFadehistorik();
    }

    // Validerer input for Fadehistorik
    private void validateGetFadehistorikInput(LagretVæske lagretVæske) {
        if (storage.getLagretVæskeById(lagretVæske.getId()) == null) {
            throw new IllegalArgumentException("LagretVæske kunne ikke findes i storage");
        }
    }
    /**-------------- Distillat METODER --------------**/

// Opretter et nyt Distillat og tilføjer det til storage
    public Distillat opretDistillat(double liter, String maltBatch, String kornsort, double alkoholprocent, String rygemateriale, LocalDate dato, String medarbejder) {
        // Opretter nyt Distillat
        Distillat distillat = new Distillat(liter, maltBatch, kornsort, alkoholprocent, rygemateriale, dato, medarbejder);
        // Tilføjer Distillat til storage
        storage.addDistillat(distillat);
        return distillat;
    }

    // Henter en liste over Distillater med væske tilbage
    public ArrayList<Distillat> getDistillaterMedActualVaeske() {
        ArrayList<Distillat> distillaterMedVæske = new ArrayList<>();
        // Gennemgår alle distillater i storage
        for (Distillat distillat : storage.getDistillater()) {
            // Tjekker om der er væske tilbage i distillatet
            if (distillat.getLiter() > 0) {
                distillaterMedVæske.add(distillat);
            }
        }
        return distillaterMedVæske;
    }

    /** ------------- WHISKY METODER ------------- **/

    // Opretter en ny Whisky og tilføjer den til storage
    public Whisky opretWhisky(String navn, LocalDate påfyldningsDato, double alkoholprocent, double liter, Flasketype flasketype, LagretVæske væske, Fad fad, String vandKilde, double fortyndelsesProcent) {
        // Beregner kapaciteten i fadet
        double fadStr = fad.getFadStr();
        double fadFyldning = fad.getFadfyldning();
        double capacity = fadStr - fadFyldning;

        // Validerer litermængden
        if (liter > fadFyldning) {
            throw new IllegalArgumentException("Så meget indeholder fadet ikke.");
        }

        // Opretter ny Whisky
        Whisky whisky = new Whisky(navn, påfyldningsDato, alkoholprocent, liter, flasketype, væske, vandKilde, fortyndelsesProcent);
        // Tilføjer Whisky til storage
        storage.addWhisky(whisky);

        // Opdaterer LagretVæske i Fad
        fad.reducereLagretVaeske(liter);

        return whisky;
    }

    // Henter fadhistorikken for en given Whisky
    public List<LagretVæskesFadHistorik> getFadHistoryForWhisky(Whisky whisky) {
        // Henter LagretVæske fra Whisky
        LagretVæske lagretVæske = whisky.getVæske();
        // Henter fadhistorikken for LagretVæske
        ArrayList<LagretVæskesFadHistorik> fadHistory = lagretVæske.getFadehistorik();
        // Liste til at holde LagretVæskesFadHistorik objekter
        List<LagretVæskesFadHistorik> fadHistoryEntries = new ArrayList<>();

        // Gennemgår fadhistorikken og opretter LagretVæskesFadHistorik objekter
        // TODO VIGTIGT!!!!
        /*for (Map.Entry<Fad, LocalDate> entry : fadHistory.entrySet()) {
            fadHistoryEntries.add(new LagretVæskesFadHistorik(entry.getKey(), entry.getValue(), LocalDate.now()));
        }*/

        return fadHistoryEntries;
    }

    // Henter en liste over alle Whisky i storage
    public ArrayList<Whisky> getWhisky() {
        // Henter listen over whiskyer
        ArrayList<Whisky> whiskyer = storage.getWhiskyer();

        // Hvis der ikke er nogen whiskyer, returner en tom liste
        if (whiskyer == null) {
            return new ArrayList<>();
        }

        return whiskyer;
    }
    public void createSomeObjects() {
        Lager l1 = controller.opretLager("Aarhus",100);
        Fad f1 = controller.opretFad("Grim",250.0, l1, 1);
        Fad f2 = controller.opretFad("Grim",30.0, l1, 2);
        Fad f3 = controller.opretFad("Grim",30.0, l1, 3);
        Fad f4 = controller.opretFad("Grim",30.0, l1,4 );
        Fad f5 = controller.opretFad("Grim",50.0, l1, 5);
        Fad f6 = controller.opretFad("Grim",50.0, l1, 6);
        ArrayList<Fad> fade = new ArrayList<>();
        fade.add(f1);
        fade.add(f2);
        fade.add(f3);
        fade.add(f4);
        fade.add(f5);
        fade.add(f6);
        Distillat d1 = controller.opretDistillat(3000,"SortBatch73","Black",40.0, "Hmm", LocalDate.of(2022,6,30), "Niels");
        Distillat d2 = controller.opretDistillat(3000,"HvidBatch39","White",40.0, "Hmm", LocalDate.of(1992,6,30), "Anders");
        Distillat d3 = controller.opretDistillat(3000,"GrønBatch41","Green",40.0, "Hmm", LocalDate.of(2000,6,30), "Kent");
        // TODO fyldPåFlereFade fix!!
        //controller.fyldPåFlereFade(250,LocalDate.of(1990,3,3), fade, d1);
        LagretVæske lV = controller.fyldPåSpecifiktFad(15,LocalDate.of(1992,6,30),f3,d1);
        LagretVæske lV2 = controller.fyldPåSpecifiktFad(15,LocalDate.of(1982,6,30),f4,d2);
        LagretVæske lV3 = controller.fyldPåSpecifiktFad(15,LocalDate.of(2020,6,30),f2,d3);


    }
}

