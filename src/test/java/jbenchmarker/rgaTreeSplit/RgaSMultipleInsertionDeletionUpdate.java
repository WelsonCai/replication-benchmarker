package jbenchmarker.rgaTreeSplit;

import static org.junit.Assert.*;

import java.io.IOException;

import jbenchmarker.core.MergeAlgorithm;
import jbenchmarker.core.SequenceOperation;
import jbenchmarker.factories.RGASplitFactory;
import jbenchmarker.factories.RGATreeSplitFactory;

import org.junit.Before;
import org.junit.Test;

import crdt.CRDTMessage;
import crdt.Factory;
import crdt.PreconditionException;
import crdt.simulator.IncorrectTraceException;
import crdt.simulator.random.StandardDiffProfile;


public class RgaSMultipleInsertionDeletionUpdate {

	private static final int REPLICA_ID = 7;
	private RgaSMerge replica;

	@Before
	public void setUp() throws Exception {
		replica = (RgaSMerge) new RGATreeSplitFactory().create(REPLICA_ID);
	}

	@Test
	public void testEmpty() {
		assertEquals("", replica.lookup());
	}

	@Test
	public void testUpdate() throws PreconditionException {
		String content = "abcdefghijk", upd = "xy";
		int pos = 3, off = 5;       
		replica.applyLocal(SequenceOperation.insert(0, content));
		assertEquals(content, replica.lookup());
		replica.applyLocal(SequenceOperation.replace(pos, off, upd));
		assertEquals(content.substring(0, pos) + upd + content.substring(pos+off), replica.lookup());        
	}

	
	@Test
	public void testConcurrentDelete() throws PreconditionException {
		String content = "abcdefghij";
		
		CRDTMessage m1 = replica.applyLocal(SequenceOperation.insert(0, content));
		assertEquals(content, replica.lookup());
		
		replica.applyLocal(SequenceOperation.insert(2, "2"));
		assertEquals("ab2cdefghij", replica.lookup());
		
		replica.applyLocal(SequenceOperation.insert(7, "7"));
		assertEquals("ab2cdef7ghij", replica.lookup());

		MergeAlgorithm replica2 = (MergeAlgorithm) new RGATreeSplitFactory().create();
		replica2.setReplicaNumber(2);
		m1.execute(replica2);
		assertEquals(content, replica2.lookup());
		
		CRDTMessage m2 = replica2.applyLocal(SequenceOperation.delete(1, 8));
		assertEquals("aj", replica2.lookup());
		
		m2.execute(replica);
		assertEquals("a27j", replica.lookup());
	}

	
	@Test
	public void testMultipleDeletions() throws PreconditionException {

		String content = "abcdefghij";
		CRDTMessage m1 = replica.applyLocal(SequenceOperation.insert(0, content));

		CRDTMessage m2 = replica.applyLocal(SequenceOperation.insert(2, "28"));
		assertEquals("ab28cdefghij", replica.lookup());

		CRDTMessage m3 = replica.applyLocal(SequenceOperation.insert(10, "73"));
		assertEquals("ab28cdefgh73ij", replica.lookup());

		CRDTMessage m4 = replica.applyLocal(SequenceOperation.delete(3, 8));
		assertEquals("ab23ij", replica.lookup());

		MergeAlgorithm replica2 = (MergeAlgorithm) new RGATreeSplitFactory().create();
		replica2.setReplicaNumber(2);
		m1.execute(replica2);
		m2.execute(replica2);
		m3.execute(replica2);		
		assertEquals("ab28cdefgh73ij", replica2.lookup());

		m4.execute(replica2);
		CRDTMessage m5 = replica2.applyLocal(SequenceOperation.insert(4, "01"));
		m5.execute(replica);
		assertEquals("ab2301ij", replica.lookup());
		assertEquals("ab2301ij", replica2.lookup());
	}

	
	@Test
	public void testMultipleUpdates() throws PreconditionException {
		String content = "abcdefghij";

		CRDTMessage m1 = replica.applyLocal(SequenceOperation.insert(0, content));
		CRDTMessage m2 = replica.applyLocal(SequenceOperation.insert(2, "2"));
		CRDTMessage m3 = replica.applyLocal(SequenceOperation.insert(7, "7"));
		assertEquals("ab2cdef7ghij", replica.lookup());

		CRDTMessage m4 = replica.applyLocal(SequenceOperation.replace(1, 10,"test"));
		assertEquals("atestj", replica.lookup());

		MergeAlgorithm replica2 = (MergeAlgorithm) new RGATreeSplitFactory().create();
		replica2.setReplicaNumber(2);
		m1.execute(replica2);
		m2.execute(replica2);
		m3.execute(replica2);
		CRDTMessage m5 =replica2.applyLocal(SequenceOperation.insert(4, "01"));
		m4.execute(replica2);
		assertEquals("atest01j", replica2.lookup()); 

		m5.execute(replica);
		assertEquals("atest01j", replica.lookup()); 
	} 


	@Test
	public void testConcurrentUpdate() throws PreconditionException{
		String content = "abcdefghij";
		CRDTMessage m1 = replica.applyLocal(SequenceOperation.insert(0, content));

		replica.applyLocal(SequenceOperation.replace(2, 4, "27"));
		assertEquals("ab27ghij", replica.lookup());

		MergeAlgorithm replica2 = (MergeAlgorithm) new RGATreeSplitFactory().create();
		replica2.setReplicaNumber(2);
		m1.execute(replica2);
		CRDTMessage m2 = replica2.applyLocal(SequenceOperation.replace(1, 8, "test"));
		m2.execute(replica);
		assertEquals("atestj", replica2.lookup());
		assertEquals("atest27j", replica.lookup());
	}

	
	
	@Test
	public void testRunRgaSplit() throws IncorrectTraceException, PreconditionException, IOException {
		StandardDiffProfile SMALL = new StandardDiffProfile(0.05, 0.8, 1, 1, 1, 1, 0.1);
		crdt.simulator.CausalDispatcherSetsAndTreesTest.testRun((Factory) new RGATreeSplitFactory.ShortList<String>(), 1000, 1000, SMALL);
	}


}
