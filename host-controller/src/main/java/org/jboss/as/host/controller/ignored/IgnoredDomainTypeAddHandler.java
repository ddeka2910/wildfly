/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.host.controller.ignored;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.host.controller.HostControllerMessages;
import org.jboss.dmr.ModelNode;

/**
 * Handles add of an ignored domain resource type.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
class IgnoredDomainTypeAddHandler implements OperationStepHandler {

    IgnoredDomainTypeAddHandler() {
    }

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {

        final String type = PathAddress.pathAddress(operation.require(ModelDescriptionConstants.OP_ADDR)).getLastElement().getValue();
        if (ModelDescriptionConstants.HOST.equals(type)) {
            throw HostControllerMessages.MESSAGES.cannotIgnoreTypeHost(ModelDescriptionConstants.HOST);
        }

        ModelNode names = IgnoredDomainTypeResourceDefinition.NAMES.validateOperation(operation);
        ModelNode wildcardNode = IgnoredDomainTypeResourceDefinition.WILDCARD.validateOperation(operation);
        Boolean wildcard = wildcardNode.isDefined() ? wildcardNode.asBoolean() : null;
        IgnoreDomainResourceTypeResource resource = new IgnoreDomainResourceTypeResource(type, names, wildcard);
        context.addResource(PathAddress.EMPTY_ADDRESS, resource);

        boolean booting = context.isBooting();
        if (!booting) {
            context.reloadRequired();
        }

        if (context.completeStep() == OperationContext.ResultAction.KEEP) {
            if (booting) {
                resource.publish();
            }
        } else if (!booting) {
            context.revertReloadRequired();
        }

    }
}
