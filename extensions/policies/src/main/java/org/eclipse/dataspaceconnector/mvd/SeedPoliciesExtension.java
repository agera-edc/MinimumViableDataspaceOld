package org.eclipse.dataspaceconnector.mvd;

import org.eclipse.dataspaceconnector.spi.contract.offer.ContractDefinitionService;
import org.eclipse.dataspaceconnector.spi.policy.RuleBindingRegistry;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import static org.eclipse.dataspaceconnector.ids.spi.policy.IdsPolicyExpressions.ABS_SPATIAL_POSITION;

/**
 * Extension to initialize the policies.
 */
public class SeedPoliciesExtension implements ServiceExtension {

    /**
     * Registry that manages rule bindings to policy scopes.
     */
    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    /**
     * Initializes the extension by binding the policies to the rule binding registry.
     *
     * @param context service extension context.
     */
    @Override
    public void initialize(ServiceExtensionContext context) {
        ruleBindingRegistry.bind("USE", ContractDefinitionService.NEGOTIATION_SCOPE);
        ruleBindingRegistry.bind(ABS_SPATIAL_POSITION, ContractDefinitionService.NEGOTIATION_SCOPE);
    }

}
