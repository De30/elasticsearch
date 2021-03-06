/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.eql.plan.physical;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.xpack.eql.session.EmptyExecutable;
import org.elasticsearch.xpack.eql.session.EqlSession;
import org.elasticsearch.xpack.eql.session.Executable;
import org.elasticsearch.xpack.eql.session.Payload;
import org.elasticsearch.xpack.ql.expression.Attribute;
import org.elasticsearch.xpack.ql.plan.logical.LogicalPlan;
import org.elasticsearch.xpack.ql.tree.NodeInfo;
import org.elasticsearch.xpack.ql.tree.NodeUtils;
import org.elasticsearch.xpack.ql.tree.Source;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public class LocalRelation extends LogicalPlan implements Executable {

    private final Executable executable;

    public LocalRelation(Source source, List<Attribute> output) {
        this(source, output, Payload.Type.EVENT);
    }

    public LocalRelation(Source source, List<Attribute> output, Payload.Type resultType) {
        this(source, new EmptyExecutable(output, resultType));
    }

    private LocalRelation(Source source, Executable executable) {
        super(source, emptyList());
        this.executable = executable;
    }

    @Override
    protected NodeInfo<LocalRelation> info() {
        return NodeInfo.create(this, LocalRelation::new, executable);
    }

    @Override
    public LogicalPlan replaceChildren(List<LogicalPlan> newChildren) {
        throw new UnsupportedOperationException("this type of node doesn't have any children to replace");
    }

    public Executable executable() {
        return executable;
    }

    @Override
    public boolean expressionsResolved() {
        return true;
    }

    @Override
    public List<Attribute> output() {
        return executable.output();
    }

    @Override
    public void execute(EqlSession session, ActionListener<Payload> listener) {
        executable.execute(session, listener);
    }

    @Override
    public int hashCode() {
        return executable.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        LocalRelation other = (LocalRelation) obj;
        return Objects.equals(executable, other.executable);
    }

    @Override
    public String nodeString() {
        return nodeName() + NodeUtils.limitedToString(output());
    }
}
