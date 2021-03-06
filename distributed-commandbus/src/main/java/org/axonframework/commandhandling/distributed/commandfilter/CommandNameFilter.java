/*
 * Copyright (c) 2010-2016. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.commandhandling.distributed.commandfilter;

import org.axonframework.commandhandling.CommandMessage;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A filter for CommandMessages which filters CommandMessages by a Command Name. It can be combined with other
 * CommandNameFilters in an efficient manner.
 *
 * @author Koen Lavooij
 */
public class CommandNameFilter implements Predicate<CommandMessage<?>>, Serializable {

    private final Set<String> commandNames;

    public CommandNameFilter(Set<String> commandNames) {
        this.commandNames = new HashSet<>(commandNames);
    }

    public CommandNameFilter(String commandName) {
        this(Collections.singleton(commandName));
    }

    @Override
    public boolean test(CommandMessage commandMessage) {
        return commandNames.contains(commandMessage.getCommandName());
    }

    @Override
    public Predicate<CommandMessage<?>> negate() {
        return new DenyCommandNameFilter(commandNames);
    }

    @Override
    public Predicate<CommandMessage<?>> and(Predicate<? super CommandMessage<?>> other) {
        if (other instanceof CommandNameFilter) {
            Set<String> otherCommandNames = ((CommandNameFilter) other).commandNames;
            return new CommandNameFilter(commandNames
                    .stream()
                    .filter(otherCommandNames::contains)
                    .collect(Collectors.toSet()));
        } else {
            return (t) -> test(t) && other.test(t);
        }
    }

    @Override
    public Predicate<CommandMessage<?>> or(Predicate<? super CommandMessage<?>> other) {
        if (other instanceof CommandNameFilter) {
            return new CommandNameFilter(
                    Stream.concat(
                            commandNames.stream(),
                            ((CommandNameFilter) other).commandNames.stream())
                            .collect(Collectors.toSet()));
        } else {
            return (t) -> test(t) || other.test(t);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandNameFilter that = (CommandNameFilter) o;
        return Objects.equals(commandNames, that.commandNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandNames);
    }

    @Override
    public String toString() {
        return "CommandNameFilter{" +
                "commandNames=" + commandNames +
                '}';
    }
}
