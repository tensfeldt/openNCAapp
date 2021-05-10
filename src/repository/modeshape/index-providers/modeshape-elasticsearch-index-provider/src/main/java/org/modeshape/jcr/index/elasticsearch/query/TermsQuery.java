/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape.jcr.index.elasticsearch.query;

import org.modeshape.jcr.index.elasticsearch.client.EsRequest;

/**
 *
 * @author kulikov
 */
public class TermsQuery extends Query {
    private final String field;
    private final Object[] value;
    
    public TermsQuery(String field, Object[] value) {
        this.field = field;
        this.value = value;
    }
    
    @Override
    public EsRequest build() {
        // EsRequest query = new EsRequest();
        // EsRequest body = new EsRequest();
        // body.put(field, value);
        // query.put("terms", body);
        // return query;

        EsRequest query = new EsRequest();
        EsRequest[] terms = new EsRequest[value.length];
        for (int i = 0, n = value.length; i < n; i++) {
            terms[i] = new EsRequest();
            EsRequest termBody = new EsRequest();
            termBody.put(field, value[i]);
            terms[i].put("match_phrase", termBody);
        }
        query.put("should", terms);
        EsRequest body = new EsRequest();
        body.put("bool", query);
        return body;
    }
    
}
