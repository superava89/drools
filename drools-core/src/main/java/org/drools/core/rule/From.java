/*
 * Copyright 2010 JBoss Inc
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

package org.drools.core.rule;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.core.WorkingMemory;
import org.drools.core.spi.DataProvider;
import org.drools.core.spi.EvalExpression;
import org.drools.core.spi.PropagationContext;
import org.drools.core.spi.Tuple;
import org.drools.core.spi.Wireable;
import org.kie.internal.security.KiePolicyHelper;

public class From extends ConditionalElement
    implements
    Externalizable,
    Wireable,
    PatternSource {

    private static final long serialVersionUID = 510l;

    private DataProvider      dataProvider;
    
    private Pattern           resultPattern;

    public From() {
    }

    public From(final DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        dataProvider    = (DataProvider)in.readObject();
        resultPattern   = ( Pattern ) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(dataProvider);
        out.writeObject(  resultPattern );
    }
    
    public void wire(Object object) {
        this.dataProvider = KiePolicyHelper.isPolicyEnabled() ? new SafeDataProvider(( DataProvider ) object) : ( DataProvider ) object;
    }

    public DataProvider getDataProvider() {
        return this.dataProvider;
    }

    public From clone() {
        return new From( this.dataProvider.clone() );
    }

    public Map getInnerDeclarations() {
        return Collections.EMPTY_MAP;
    }

    public Map getOuterDeclarations() {
        return Collections.EMPTY_MAP;
    }

    /**
     * @inheritDoc
     */
    public Declaration resolveDeclaration(final String identifier) {
        return null;
    }

    public List getNestedElements() {
        return Collections.EMPTY_LIST;
    }

    public boolean isPatternScopeDelimiter() {
        return true;
    }

    public void setResultPattern(Pattern pattern) {
        this.resultPattern = pattern;
    }
    
    public Pattern getResultPattern() {
        return this.resultPattern;
    }
    
    private static class SafeDataProvider implements DataProvider {
        private DataProvider delegate;
        public SafeDataProvider(DataProvider delegate) {
            this.delegate = delegate;
        }

        @Override
        public Declaration[] getRequiredDeclarations() {
            return delegate.getRequiredDeclarations();
        }

        @Override
        public Object createContext() {
            return AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    return delegate.createContext();
                }
            }, KiePolicyHelper.getAccessContext());
        }

        @Override
        public Iterator getResults(final Tuple tuple, 
                final WorkingMemory wm, 
                final PropagationContext ctx, 
                final Object providerContext) {
            return AccessController.doPrivileged(new PrivilegedAction<Iterator>() {
                @Override
                public Iterator run() {
                    return delegate.getResults(tuple, wm, ctx, providerContext);
                }
            }, KiePolicyHelper.getAccessContext());
        }

        @Override
        public void replaceDeclaration(Declaration declaration, Declaration resolved) {
            delegate.replaceDeclaration(declaration, resolved);
        }
        
        @Override
        public SafeDataProvider clone() {
            return new SafeDataProvider( delegate.clone() );
        }
    }

}
