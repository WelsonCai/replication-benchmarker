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
 
package crdt.simulator.sizecalculator;

import crdt.CRDT;
import java.io.IOException;

/**
 *
 * @author Stephane Martin <stephane@stephanemartin.fr>
 */
public interface SizeCalculator {
    /**
     * Compute the size of an object
     * @param m the object to be sized
     * @return the computed of the object
     * @throws java.io.IOException 
     */
    public long serializ(CRDT m) throws IOException;
    
}
