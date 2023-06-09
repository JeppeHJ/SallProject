package UnitTest;

import application.Distillat;
import application.Fad;
import application.LagretVæske;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LagretVæskeTest {

    @Test
    void testAddDestillater() {
        LagretVæske lagretVæske = new LagretVæske(100, LocalDate.now());
        Distillat distillat1 = new Distillat(  50,"tw21","test1", 50, "kos", LocalDate.now(), "cme");
        Distillat distillat2 = new Distillat(50,"tw21","test2", 50, "kos", LocalDate.now(), "cme");

        ArrayList<Distillat> distillater = new ArrayList<>(Arrays.asList(distillat1, distillat2));
        lagretVæske.addDestillater(distillater);

        assertEquals(2, lagretVæske.getDistillater().size());
        assertTrue(lagretVæske.getDistillater().contains(distillat1));
        assertTrue(lagretVæske.getDistillater().contains(distillat2));
    }


    @Test
    void testEditHistoryWhenOmhældning() {
        LagretVæske lagretVæske1 = new LagretVæske(100, LocalDate.now());
        LagretVæske lagretVæske2 = new LagretVæske(100, LocalDate.now());
        Fad fad = new Fad("Fad 1", 200);
        LocalDate fillDate = LocalDate.now().minusDays(10);
        LocalDate emptyDate = LocalDate.now().minusDays(5);

        lagretVæske1.addFadTilHistorik(fad, fillDate, null);
        fad.addLagretVæsker(lagretVæske1);
        lagretVæske1.editHistoryNårDerOmhældes(lagretVæske2, emptyDate);

        assertEquals(1, lagretVæske1.getFadehistorik().size());
        assertEquals(null, lagretVæske1.getFadehistorik().get(0).getTilDato());
    }

    @Test
    void testAddFadTilHistorik() {
        LagretVæske lagretVæske = new LagretVæske(100, LocalDate.now());
        Fad fad = new Fad("Fad 1", 200);
        LocalDate fillDate = LocalDate.now().minusDays(10);
        LocalDate emptyDate = LocalDate.now().minusDays(5);

        lagretVæske.addFadTilHistorik(fad, fillDate, emptyDate);

        assertEquals(1, lagretVæske.getFadehistorik().size());
        assertTrue(lagretVæske.getFadehistorik().get(0).getFad().equals(fad));
        assertEquals(fillDate, lagretVæske.getFadehistorik().get(0).getFraDato());
        assertEquals(emptyDate, lagretVæske.getFadehistorik().get(0).getTilDato());
    }

    @Test
    void testCalculateNewAlkoholProcent() {
        Distillat distillat1 = new Distillat(  50,"tw21","test1", 50, "kos", LocalDate.now(), "cme");
        Distillat distillat2 = new Distillat(50,"tw21","test2", 50, "kos", LocalDate.now(), "cme");

        ArrayList<Distillat> distillater = new ArrayList<>(Arrays.asList(distillat1, distillat2));
        double[] volumes = {50, 50};

        double expectedAlkoholProcent = (distillat1.getAlkoholprocent() * volumes[0] + distillat2.getAlkoholprocent() * volumes[1]) / (volumes[0] + volumes[1]);
        double actualAlkoholProcent = LagretVæske.calculateNewAlkoholProcent(distillater, volumes);

        assertEquals(expectedAlkoholProcent, actualAlkoholProcent);
    }
}

