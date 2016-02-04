/**
 * Replication Benchmarker
 * https://github.com/score-team/replication-benchmarker/
 * Copyright (C) 2013 LORIA / Inria / SCORE Team
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

import crdt.CRDT;
import crdt.simulator.CausalSimulator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import jbenchmarker.core.MergeAlgorithm;
import jbenchmarker.core.ReplicaFactory;
import jbenchmarker.trace.TraceGenerator;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author mzawirski
 */
public abstract class AbstractIntegrationTest {
	protected CausalSimulator cd;

	@Before
	public void setUp() {
		cd = new CausalSimulator(createFactory(),  false, 0);
	}

	protected abstract ReplicaFactory createFactory();

    protected void assertConsistentViews(CausalSimulator cd) {
        String referenceView = null;
        for (final CRDT replica : cd.getReplicas().values()) {
            final String view = ((MergeAlgorithm) replica).lookup();
            if (referenceView == null) {
                referenceView = view;
            } else {
                assertEquals(referenceView, view);
            }
        }
        assertNotNull(referenceView);
    }
        
	@Test
	public void testExempleRun() throws Exception {
		cd.run(TraceGenerator.traceFromXML(TracesExample.getExampleTraceMatch("exemple.xml"), 1));
		assertConsistentViews(cd);
	}

	@Test
	public void testG1Run() throws Exception {
		cd.run(TraceGenerator.traceFromXML(TracesExample.getExampleTraceMatch("G1.xml"), 1));
		assertConsistentViews(cd);
	}

	@Test
	public void testG2Run() throws Exception {
		cd.run(TraceGenerator.traceFromXML(TracesExample.getExampleTraceMatch("G2.xml"), 1, 16));
		assertConsistentViews(cd);
	}

	@Test
	public void testG3Run() throws Exception {
		cd.run(TraceGenerator.traceFromXML(TracesExample.getExampleTraceMatch("G3.xml"), 1));
		assertConsistentViews(cd);
	}

	@Test
	public void testSerieRun() throws Exception {
		cd.run(TraceGenerator.traceFromXML(TracesExample.getExampleTraceMatch("Serie.xml"), 1));
		assertConsistentViews(cd);
	}
        
        
        

}
