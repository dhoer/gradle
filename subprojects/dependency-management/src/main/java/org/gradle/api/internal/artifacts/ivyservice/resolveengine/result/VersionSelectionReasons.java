/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.result;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.gradle.api.artifacts.result.ComponentSelectionCause;
import org.gradle.api.artifacts.result.ComponentSelectionDescriptor;
import org.gradle.api.artifacts.result.ComponentSelectionReason;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;

public class VersionSelectionReasons {
    public static final ComponentSelectionDescriptorInternal REQUESTED = new DefaultComponentSelectionDescriptor(ComponentSelectionCause.REQUESTED);
    public static final ComponentSelectionDescriptorInternal ROOT = new DefaultComponentSelectionDescriptor(ComponentSelectionCause.ROOT);
    public static final ComponentSelectionDescriptorInternal FORCED = new DefaultComponentSelectionDescriptor(ComponentSelectionCause.FORCED);
    public static final ComponentSelectionDescriptorInternal CONFLICT_RESOLUTION = new DefaultComponentSelectionDescriptor(ComponentSelectionCause.CONFLICT_RESOLUTION);
    public static final ComponentSelectionDescriptorInternal SELECTED_BY_RULE = new DefaultComponentSelectionDescriptor(ComponentSelectionCause.SELECTED_BY_RULE);
    public static final ComponentSelectionDescriptorInternal COMPOSITE_BUILD = new DefaultComponentSelectionDescriptor(ComponentSelectionCause.COMPOSITE_BUILD);

    public static ComponentSelectionReasonInternal requested() {
        return new DefaultComponentSelectionReason(REQUESTED);
    }


    public static ComponentSelectionReason root() {
        return new DefaultComponentSelectionReason(ROOT);
    }

    public static ComponentSelectionReasonInternal of(List<? extends ComponentSelectionDescriptor> descriptions) {
        return new DefaultComponentSelectionReason(descriptions);
    }

    public static ComponentSelectionReasonInternal of(ComponentSelectionDescriptor descriptions) {
        return new DefaultComponentSelectionReason(descriptions);
    }

    private static class DefaultComponentSelectionReason implements ComponentSelectionReasonInternal {

        private final ArrayDeque<ComponentSelectionDescriptorInternal> descriptions;

        private DefaultComponentSelectionReason(ComponentSelectionDescriptor description) {
            descriptions = new ArrayDeque<ComponentSelectionDescriptorInternal>(1);
            descriptions.add((ComponentSelectionDescriptorInternal) description);
        }

        public DefaultComponentSelectionReason(List<? extends ComponentSelectionDescriptor> descriptions) {
            this.descriptions = new ArrayDeque<ComponentSelectionDescriptorInternal>(1);
            for (ComponentSelectionDescriptor description : descriptions) {
                addCause(description);
            }
        }


        public boolean isForced() {
            return hasCause(ComponentSelectionCause.FORCED);
        }

        private boolean hasCause(ComponentSelectionCause cause) {
            for (ComponentSelectionDescriptor description : descriptions) {
                if (description.getCause() == cause) {
                    return true;
                }
            }
            return false;
        }

        public boolean isConflictResolution() {
            return hasCause(ComponentSelectionCause.CONFLICT_RESOLUTION);
        }

        public boolean isSelectedByRule() {
            return hasCause(ComponentSelectionCause.SELECTED_BY_RULE);
        }

        public boolean isExpected() {
            ComponentSelectionCause cause = Iterables.getLast(descriptions).getCause();
            return cause == ComponentSelectionCause.ROOT || cause == ComponentSelectionCause.REQUESTED;
        }

        public boolean isCompositeSubstitution() {
            return hasCause(ComponentSelectionCause.COMPOSITE_BUILD);
        }

        public String getDescription() {
            // for backwards compatibility, we use the last added description
            return descriptions.getLast().toString();
        }

        public String toString() {
            return getDescription();
        }

        @Override
        public ComponentSelectionReasonInternal addCause(ComponentSelectionCause cause, String description) {
            addCause(new DefaultComponentSelectionDescriptor(cause, description));
            return this;
        }


        @Override
        public ComponentSelectionReasonInternal setCause(ComponentSelectionDescriptor description) {
            descriptions.clear();
            addCause(description);
            return this;
        }

        @Override
        public ComponentSelectionReasonInternal addCause(ComponentSelectionDescriptor description) {
            if (!descriptions.contains(description)) {
                descriptions.add((ComponentSelectionDescriptorInternal) description);
            }
            return this;
        }

        @Override
        public List<ComponentSelectionDescriptor> getDescriptions() {
            return ImmutableList.<ComponentSelectionDescriptor>copyOf(descriptions);
        }

        @Override
        public boolean hasCustomDescriptions() {
            for (ComponentSelectionDescriptorInternal description : descriptions) {
                if (description.hasCustomDescription()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DefaultComponentSelectionReason that = (DefaultComponentSelectionReason) o;
            return sameDescriptorsAs(that);
        }

        private boolean sameDescriptorsAs(DefaultComponentSelectionReason that) {
            if (descriptions.size() != that.descriptions.size()) {
                return false;
            }
            Iterator<ComponentSelectionDescriptorInternal> it1 = descriptions.iterator();
            Iterator<ComponentSelectionDescriptorInternal> it2 = descriptions.iterator();
            while (it1.hasNext()) {
                if (!it1.next().equals(it2.next())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(descriptions);
        }
    }
}
