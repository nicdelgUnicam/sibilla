/**
 * generated by Xtext 2.17.0
 */
package quasylab.sibilla.lang.pm;

import quasylab.sibilla.lang.pm.ModelStandaloneSetupGenerated;

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
@SuppressWarnings("all")
public class ModelStandaloneSetup extends ModelStandaloneSetupGenerated {
  public static void doSetup() {
    new ModelStandaloneSetup().createInjectorAndDoEMFRegistration();
  }
}