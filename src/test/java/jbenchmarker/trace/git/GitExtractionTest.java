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
package jbenchmarker.trace.git;

import java.net.MalformedURLException;
import jbenchmarker.core.SequenceOperation;
import jbenchmarker.trace.git.model.Commit;
import jbenchmarker.trace.git.model.Edition;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbPath;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.junit.Test;
import static org.junit.Assert.*;
import static collect.Utils.*;
import jbenchmarker.core.SequenceOperation.OpType;
import java.util.List;
import org.junit.Ignore;

/**
 *
 * @author urso
 */
public class GitExtractionTest {

    public static boolean sorted(List<Edition> l) {
        int m = Integer.MAX_VALUE;
        for (Edition e : l) {
            if (e.getBeginA() > m || (e.getBeginA() == m && e.getType() != OpType.insert)) {
                return false;
            }
            m = e.getBeginA();
        }
        return true;
    }
    
    @Test
    public void storeCommitTest() throws MalformedURLException {
        HttpClient httpClient = new StdHttpClient.Builder().url("http://localhost:5984").build();

        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        if (dbInstance.checkIfDbExists(new DbPath("test"))) {
            dbInstance.deleteDatabase("test");
        }
        CouchDbConnector db = new StdCouchDbConnector("test", dbInstance);
        db.createDatabaseIfNotExists();

        CommitCRUD commitCRUD = new CommitCRUD(db);
        
        Commit commit = new Commit();
        commit.setId("myid");
        commit.setMessage("coucou");
        commit.setReplica(42);
        commitCRUD.add(commit);
        
        Commit result = commitCRUD.get("myid");
        assertEquals("myid", result.getId());
        assertEquals("coucou", result.getMessage());
        assertEquals(42, result.getReplica());
        
        Commit result2 = commitCRUD.getAll().get(0);
        assertEquals("myid", result2.getId());
        assertEquals("coucou", result2.getMessage());
        assertEquals(42, result2.getReplica());
    }
    
    @Ignore
    @Test
    public void storeAnrRetrieve() throws MalformedURLException {
        HttpClient httpClient = new StdHttpClient.Builder().url("http://localhost:5984").build();

        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        if (dbInstance.checkIfDbExists(new DbPath("test_commit"))) {
            dbInstance.deleteDatabase("test_commit");
        }
        CouchDbConnector db = new StdCouchDbConnector("test", dbInstance);
        db.createDatabaseIfNotExists();
        
        fail("Not implemented yet");
    }
    
    String A = "AAAAAAAAAAAAAAAAA", 
            B= "BBBBBBBBBBBBBBBBB",
            C= "CCCCCCCCCCCCCCCCC",
            X= "XXXXXXXXXXXXXXXXX",
            Y= "YYYYYYYYYYYYYYYYY",
            Z= "ZZZZZZZZZZZZZZZZZ",
            Aa = "AAAAAAAAAAAAAAAAAx", 
            Ba = "BBBBBBBBBBBBBBBBBx";
    
    @Test 
    public void detectNone() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.replace, 42, 33, 0, toList(A, B), toList(X, Y, Z)),
                r1 = new Edition(OpType.delete, 42, 33, 0, toList(A, B), null),
                r2 = new Edition(OpType.insert, 42, 33, 0, null, toList(X, Y, Z));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e));
        
        assertEquals(toList(r1, r2), result);
    }
    
    @Test 
    public void detectUpdate() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.replace, 42, 33, 0, toList(A, B), toList(Aa, Ba)),
                r = new Edition(OpType.update, 42, 33, 0, toList(A, B), toList(Aa, Ba));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e));
        
        assertEquals(toList(r), result);
    }
    
    @Test 
    public void detectPartialUpdate() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.delete, 55, 43, 0, toList(X), null),
                f = new Edition(OpType.replace, 42, 33, 0, toList(A, B), toList(Y, Aa, Z)),
                
                r1 = new Edition(OpType.delete, 55, 43, 0, toList(X), null),
                r2 = new Edition(OpType.delete, 43, 36, 0, toList(B), null),
                r3 = new Edition(OpType.insert, 43, 35, 0, null, toList(Z)),
                r4 = new Edition(OpType.update, 42, 34, 0, toList(A), toList(Aa)),
                r5 = new Edition(OpType.insert, 42, 33, 0, null, toList(Y));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f));

        assertTrue(sorted(result));
        assertEquals(toList(r1, r2, r3, r4, r5), result);
    }

    @Test 
    public void detectPureMove() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.delete, 55, 43, 0, toList(A, B), null),
                f = new Edition(OpType.insert, 42, 33, 0, null, toList(A, B)),
                
                r = new Edition(OpType.move, 55, 33, 42, toList(A, B), toList(A, B));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f));

        assertEquals(toList(r), result);
    }
    
    @Test 
    public void detectMove() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.delete, 55, 43, 0, toList(A, Ba), null),
                f = new Edition(OpType.insert, 42, 33, 0, null, toList(Aa, B)),
                
                r = new Edition(OpType.move, 55, 33, 42, toList(A, Ba), toList(Aa, B));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f));

        assertEquals(toList(r), result);
    }
    
    @Test 
    public void detectPartialMoveDown() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.replace, 55, 43, 0, toList(X, A, Ba, Y), toList(Z)),
                f = new Edition(OpType.insert, 42, 33, 0, null, toList(C, A, B, C)),
                
                r1 = new Edition(OpType.delete, 58, 43, 0, toList(Y), null), 
                r2 = new Edition(OpType.move, 56, 33, 42, toList(A, Ba), toList(A, B)),
                r3 = new Edition(OpType.delete, 57, 43, 0, toList(X), null),
                r4 = new Edition(OpType.insert, 57, 43, 0, null, toList(Z)),
                r5 = new Edition(OpType.insert, 44, 36, 0, null, toList(C)),
                r6 = new Edition(OpType.insert, 42, 33, 0, null, toList(C));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f));

        assertEquals(toList(r1, r2, r3, r4, r5, r6), result);
    }

    @Test 
    public void detectPartialMoveUp() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.replace, 55, 73, 0, toList(X, A, Ba, Y), toList(Z)),
                f = new Edition(OpType.insert, 62, 83, 0, null, toList(A, B, C, Z)),
                
                r0 = new Edition(OpType.insert, 62, 85, 0, null, toList(C, Z)),
                r1 = new Edition(OpType.delete, 58, 73, 0, toList(Y), null),
                r2 = new Edition(OpType.move, 56, 83, 59, toList(A, Ba), toList(A, B)),
                r3 = new Edition(OpType.delete, 55, 73, 0, toList(X), null),
                r4 = new Edition(OpType.insert, 55, 73, 0, null, toList(Z));
        List<Edition> result = ge.detectMovesAndUpdates(toList(f, e));

        assertEquals(toList(r0, r1, r2, r3, r4), result);
    }
    
    @Test 
    public void detectCrossMoveOut() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.insert, 9, 6, 0, null, toList(B, B, Ba)),
                f = new Edition(OpType.delete, 6, 5, 0, toList(Aa, A), null),
                g = new Edition(OpType.delete, 2, 4, 0, toList(Ba, B, B), null),
                h = new Edition(OpType.insert, 1, 1, 0, null, toList(A, A)),
                
                r0 = new Edition(OpType.move, 6, 1, 1, toList(Aa, A), toList(A, A)),
                r1 = new Edition(OpType.move, 4, 6, 6, toList(Ba, B, B), toList(B, B, Ba));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f, g, h));

        assertEquals(toList(r0, r1), result);
    }

    @Test 
    public void detectCrossMoveIn() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.delete, 6, 9, 0, toList(B, B, Ba), null),
                f = new Edition(OpType.insert, 5, 6, 0, null, toList(Aa, A)),
                g = new Edition(OpType.insert, 4, 2, 0, null, toList(Ba, B, B)),
                h = new Edition(OpType.delete, 1, 1, 0, toList(A, A), null),
                
                r0 = new Edition(OpType.move, 6, 2, 4, toList(B, B, Ba), toList(Ba, B, B)),
                r1 = new Edition(OpType.move, 1, 6, 6, toList(A, A), toList(Aa, A));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f, g, h));

        assertEquals(toList(r0, r1), result);
    }
        

    @Test 
    public void detectCrossMoveDown() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.delete, 6, 9, 0, toList(B, B, Ba), null),
                f = new Edition(OpType.delete, 3, 8, 0, toList(A, A), null),
                g = new Edition(OpType.insert, 2, 4, 0, null, toList(Ba, B, B)),
                h = new Edition(OpType.insert, 1, 1, 0, null, toList(Aa, A)),
                
                r0 = new Edition(OpType.move, 6, 4, 2, toList(B, B, Ba), toList(Ba, B, B)),
                r1 = new Edition(OpType.move, 6, 1, 1, toList(A, A), toList(Aa, A));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f, g, h));

        assertEquals(toList(r0, r1), result);
    }
    
    @Test 
    public void detectCrossMoveUp() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.insert, 9, 6, 0, null, toList(Ba, B, B)),
                f = new Edition(OpType.insert, 8, 3, 0, null, toList(Aa, A)),
                g = new Edition(OpType.delete, 4, 2, 0, toList(B, B, Ba), null),
                h = new Edition(OpType.delete, 1, 1, 0, toList(A, A), null),
                
                r0 = new Edition(OpType.move, 4, 6, 6, toList(B, B, Ba), toList(Ba, B, B)),
                r1 = new Edition(OpType.move, 1, 3, 3, toList(A, A), toList(Aa, A));
        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f, g, h));

        assertEquals(toList(r0, r1), result);
    }
    
    @Test 
    public void detectCrossMove() {
        GitExtraction ge = new GitExtraction(20, 10);
        Edition e = new Edition(OpType.replace, 75, 76, 0, toList(C, C, C), toList(X, X)),
                f = new Edition(OpType.insert, 62, 61, 0, null, toList(Aa, A)),
                g = new Edition(OpType.replace, 57, 56, 0, toList(X, X), toList(B, Ba)),
                h = new Edition(OpType.replace, 42, 42, 0, toList(A, A, B, B), toList(C, C, C)),
                
                r0 = new Edition(OpType.move, 75, 42, 42, toList(C, C, C), toList(C, C, C)),
                r1 = new Edition(OpType.move, 60, 76, 76, toList(X, X), toList(X, X)),
                r2 = new Edition(OpType.move, 47, 56, 58, toList(B, B), toList(B, Ba)),
                r3 = new Edition(OpType.move, 45, 61, 61, toList(A, A), toList(Aa, A));

        List<Edition> result = ge.detectMovesAndUpdates(toList(e, f, g, h));

        assertEquals(toList(r0, r1, r2, r3), result);
    }
}