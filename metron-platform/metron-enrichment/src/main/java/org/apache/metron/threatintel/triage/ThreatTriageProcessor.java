/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.metron.threatintel.triage;

import com.google.common.base.Function;
import org.apache.metron.common.configuration.enrichment.SensorEnrichmentConfig;
import org.apache.metron.common.configuration.enrichment.threatintel.ThreatIntelConfig;
import org.apache.metron.common.configuration.enrichment.threatintel.ThreatTriageConfig;
import org.apache.metron.common.dsl.*;
import org.apache.metron.common.dsl.functions.resolver.FunctionResolver;
import org.apache.metron.common.stellar.StellarPredicateProcessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThreatTriageProcessor implements Function<Map, Double> {
  private SensorEnrichmentConfig sensorConfig;
  private ThreatIntelConfig threatIntelConfig;
  private ThreatTriageConfig threatTriageConfig;
  private Context context;
  private FunctionResolver functionResolver;
  public ThreatTriageProcessor( SensorEnrichmentConfig config
                              , FunctionResolver functionResolver
                              , Context context
                              )
  {
    this.threatIntelConfig = config.getThreatIntel();
    this.sensorConfig = config;
    this.threatTriageConfig = config.getThreatIntel().getTriageConfig();
    this.functionResolver = functionResolver;
    this.context = context;
  }

  @Nullable
  @Override
  public Double apply(@Nullable Map input) {
    List<Number> scores = new ArrayList<>();
    StellarPredicateProcessor predicateProcessor = new StellarPredicateProcessor();
    VariableResolver resolver = new MapVariableResolver(input, sensorConfig.getConfiguration(), threatIntelConfig.getConfig());
    for(Map.Entry<String, Number> kv : threatTriageConfig.getRiskLevelRules().entrySet()) {
      if(predicateProcessor.parse(kv.getKey(), resolver, functionResolver, context)) {
        scores.add(kv.getValue());
      }
    }
    return threatTriageConfig.getAggregator().aggregate(scores, threatTriageConfig.getAggregationConfig());
  }
}
