/**
 * Replication Benchmarker
 * https://github.com/score-team/replication-benchmarker/
 * Copyright (C) 2011 INRIA / LORIA / SCORE Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jbenchmarker.sim;

import jbenchmarker.TraceSimul2XML;
import org.junit.Ignore;
import crdt.CRDT;
import crdt.simulator.Trace;
import crdt.simulator.random.RandomTrace;
import crdt.simulator.random.StandardSeqOpProfile;
import crdt.simulator.CausalSimulator;
import java.util.logging.Logger;
import jbenchmarker.factories.TTFFactories;
import jbenchmarker.trace.TraceGenerator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author urso
 */
public class IntegrationSOCT2 {
   
    @Test
    public void testSOCT2ExempleRun() throws Exception {
        System.out.println("Integration test with WootH");
        Trace trace = TraceGenerator.traceFromXML("../../traces/xml/exemple.xml", 1);
        CausalSimulator cd = new CausalSimulator(new TTFFactories.WithoutGC());

        cd.run(trace, false);
        String r = "Salut Monsieurjour MehdiFin";
        assertEquals(r, cd.getReplicas().get(0).lookup());
        assertEquals(r, cd.getReplicas().get(2).lookup());
        assertEquals(r, cd.getReplicas().get(4).lookup());
    }
    
    @Ignore   // 231,986 s  on rev 105
    @Test
    public void testSOCT2RunG1() throws Exception {
        Trace trace = TraceGenerator.traceFromXML("../../traces/xml/G1.xml", 1);         
        CausalSimulator cd = new CausalSimulator(new TTFFactories.WithoutGC());

        long startTime = System.currentTimeMillis();
        cd.run(trace, false);
        long endTime = System.currentTimeMillis();
         
        Logger.getLogger(getClass().getCanonicalName()).info("computation time: "+(endTime-startTime)+" ms");
        
        String r = (String) cd.getReplicas().get(0).lookup();
        for (CRDT m : cd.getReplicas().values()) {
            assertEquals(r, m.lookup());
        }
    }
    
    @Test
    public void testSOCT2RunJSON() throws Exception {
        Trace trace = TraceGenerator.traceFromJson("/home/damien/etherpad-lite/var/dirtyCS.db","test");
        //Trace trace = TraceGenerator.traceFromJson("../../traces/json/dirtyCS.db");
        //Trace trace = TraceGenerator.traceFromJson("../../traces/json/dirtyCSGerald3.db","notes001");//pb avec notes001, notes002 corrompu a cause d'un pb lors du test des etudiants
        CausalSimulator cd = new CausalSimulator(new TTFFactories.WithoutGC());
        //CausalSimulator cd = new CausalSimulator(new CausalCheckerFactory());

        long startTime = System.currentTimeMillis();
        cd.run(trace, false);        
        long endTime = System.currentTimeMillis();
         
        Logger.getLogger(getClass().getCanonicalName()).info("computation time: "+(endTime-startTime)+" ms");
        
        
        String r = (String) cd.getReplicas().get(0).lookup();        
        for (CRDT m : cd.getReplicas().values()) {
            assertEquals(r, m.lookup());
        } 
        
        //System.out.println(r);
        
        
    }
    
    @Ignore
    @Test
    public void testSOCT2Random() throws Exception {
        Trace trace = new RandomTrace(2000, RandomTrace.FLAT, new StandardSeqOpProfile(0.8, 0.1, 40, 5.0), 0.1, 10, 3.0, 13);
        CausalSimulator cd = new CausalSimulator(new TTFFactories.WithoutGC());

        cd.run(trace, false);
        String r = (String) cd.getReplicas().get(0).lookup();
        for (CRDT m : cd.getReplicas().values()) {
            assertEquals(r, m.lookup());
        }     
    }

    @Ignore
    @Test
    public void testSOCT2SimulXML() throws Exception {
        Trace trace = new RandomTrace(2000, RandomTrace.FLAT, new StandardSeqOpProfile(0.8, 0.1, 40, 5.0), 0.1, 10, 3.0, 5);
        CausalSimulator cdSim = new CausalSimulator(new TTFFactories.WithoutGC());
        cdSim.setLogging("trace.log");
        cdSim.run(trace, false);
         
        TraceSimul2XML mn = new TraceSimul2XML();
        String[] args = new String[]{"trace.log", "trace.xml"};
        mn.main(args);
        
        Trace real = TraceGenerator.traceFromXML("trace.xml", 1);
        CausalSimulator cdReal = new CausalSimulator(new TTFFactories.WithoutGC());
        cdReal.run(real, true);
        
        //compare all replica
        for (CRDT crdtSim : cdSim.getReplicas().values()) {
            for (CRDT crdtReal : cdReal.getReplicas().values()) {
                assertEquals(crdtSim.lookup(), crdtReal.lookup());
            }
        }
    }
    
}
