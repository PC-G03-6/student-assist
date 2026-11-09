package ro.studentassist;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifică respectarea granițelor modulare conform docs/architecture.md
 * (fără dependențe ciclice și fără acces direct la clasele interne ale altor module).
 */
class ModularityTests {

    @Test
    void verifiesModularArchitecture() {
        ApplicationModules modules = ApplicationModules.of(BackendApplication.class);
        modules.verify();
    }
}
