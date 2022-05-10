package org.eclipse.dataspaceconnector.mvd;

import org.eclipse.dataspaceconnector.spi.contract.offer.ContractDefinitionService;
import org.eclipse.dataspaceconnector.spi.policy.RuleBindingRegistry;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import static org.eclipse.dataspaceconnector.ids.spi.policy.IdsPolicyExpressions.ABS_SPATIAL_POSITION;

public class SeedPoliciesExtension implements ServiceExtension {

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Override
    public void initialize(ServiceExtensionContext context) {
        ruleBindingRegistry.bind("USE", ContractDefinitionService.NEGOTIATION_SCOPE);
        ruleBindingRegistry.bind(ABS_SPATIAL_POSITION, ContractDefinitionService.NEGOTIATION_SCOPE);
    }

}
