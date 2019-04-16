package ca.uhn.hl7v2.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;

/**
 * Iterates over all defined nodes (ie segments, groups) in a message,
 * regardless of whether they have been instantiated previously. This is a
 * tricky process, because the number of nodes is infinite, due to infinitely
 * repeating segments and groups. See <code>next()</code> for details on how
 * this is handled.
 * 
 * This implementation assumes that the first segment in each group is present
 * (as per HL7 rules). Specifically, when looking for a segment location, an
 * empty group that has a spot for the segment will be overlooked if there is
 * anything else before that spot. This may result in surprising (but sensible)
 * behaviour if a message is missing the first segment in a group.
 * 
 * @author Bryan Tripp
 */
public class MessageIterator implements java.util.Iterator<Structure> {

    private Message myMessage;
    private String myDirection;
    private boolean myNextIsSet;
    private boolean myHandleUnexpectedSegments;
    private List<Position> myCurrentDefinitionPath = new ArrayList<Position>();

    private static final Logger log = LoggerFactory.getLogger(MessageIterator.class);

    /*
     * may add configurability later ... private boolean findUpToFirstRequired;
     * private boolean findFirstDescendentsOnly;
     * 
     * public static final String WHOLE_GROUP; public static final String
     * FIRST_DESCENDENTS_ONLY; public static final String UP_TO_FIRST_REQUIRED;
     */

    /** Creates a new instance of MessageIterator */
    public MessageIterator(Message start, IStructureDefinition startDefinition, String direction, boolean handleUnexpectedSegments) {
        this.myMessage = start;
        this.myDirection = direction;
        this.myHandleUnexpectedSegments = handleUnexpectedSegments;
        this.myCurrentDefinitionPath.add(new Position(startDefinition, -1));
    }

    private Position getCurrentPosition() {
        return getTail(myCurrentDefinitionPath);
    }

    private Position getTail(List<Position> theDefinitionPath) {
        return theDefinitionPath.get(theDefinitionPath.size() - 1);
    }

    private List<Position> popUntilMatchFound(List<Position> theDefinitionPath) {
        theDefinitionPath = new ArrayList<Position>(theDefinitionPath.subList(0, theDefinitionPath.size() - 1));

        Position newCurrentPosition = getTail(theDefinitionPath);
        IStructureDefinition newCurrentStructureDefinition = newCurrentPosition.getStructureDefinition();

        if (newCurrentStructureDefinition.getAllPossibleFirstChildren().contains(myDirection)) {
            return theDefinitionPath;
        }

        if (newCurrentStructureDefinition.isFinalChildOfParent()) {
            if (theDefinitionPath.size() > 1) {
                return popUntilMatchFound(theDefinitionPath); // recurse
            } else {
            	log.debug("Popped to root of message and did not find a match for {}", myDirection);
                return null;
            }
        }

        return theDefinitionPath;
    }

    /**
     * Returns true if another object exists in the iteration sequence.
     */
    public boolean hasNext() {

        log.trace("hasNext() for direction {}", myDirection);
        if (myDirection == null) {
            throw new IllegalStateException("Direction not set");
        }

        while (!myNextIsSet) {

            Position currentPosition = getCurrentPosition();

            log.trace("hasNext() current position: {}", currentPosition);

            IStructureDefinition structureDefinition = currentPosition.getStructureDefinition();
            
            if (myMessage.getParser().getParserConfiguration().isNonGreedyMode()) {
            	IStructureDefinition nonGreedyPosition = couldBeNotGreedy();
            	if (nonGreedyPosition != null) {
            		log.info("Found non greedy parsing choice, moving to {}", nonGreedyPosition.getName());
            		while (getCurrentPosition().getStructureDefinition() != nonGreedyPosition) {
            			myCurrentDefinitionPath.remove(myCurrentDefinitionPath.size() - 1);
            		}
            	}
            }
            
            if (structureDefinition.isSegment() && structureDefinition.getName().startsWith(myDirection) && (structureDefinition.isRepeating() || currentPosition.getRepNumber() == -1)) {
                myNextIsSet = true;
                currentPosition.incrementRep();
            } else if (structureDefinition.isSegment() && structureDefinition.getNextLeaf() == null
                    && !structureDefinition.getNamesOfAllPossibleFollowingLeaves().contains(myDirection)) {
                if (!myHandleUnexpectedSegments) {
                    return false;
                }
                addNonStandardSegmentAtCurrentPosition();
            } else if (structureDefinition.hasChildren() && structureDefinition.getAllPossibleFirstChildren().contains(myDirection) && (structureDefinition.isRepeating() || currentPosition.getRepNumber() == -1)) {
                currentPosition.incrementRep();
                myCurrentDefinitionPath.add(new Position(structureDefinition.getFirstChild(), -1));
            } else if (!structureDefinition.hasChildren() && !structureDefinition.getNamesOfAllPossibleFollowingLeaves().contains(myDirection)) {
                if (!myHandleUnexpectedSegments) {
                    return false;
                }
                addNonStandardSegmentAtCurrentPosition();
                // } else if (structureDefinition.isMessage()) {
                // if (!handleUnexpectedSegments) {
                // return false;
                // }
                // addNonStandardSegmentAtCurrentPosition();
            } else if (structureDefinition.isFinalChildOfParent()) {
                List<Position> newDefinitionPath = popUntilMatchFound(myCurrentDefinitionPath);
                if (newDefinitionPath != null) {
                    // found match
                    myCurrentDefinitionPath = newDefinitionPath;
                } else {
                    if (!myHandleUnexpectedSegments) {
                        return false;
                    }
                    addNonStandardSegmentAtCurrentPosition();
                }
            } else {
                currentPosition.setStructureDefinition(structureDefinition.getNextSibling());
                currentPosition.resetRepNumber();
            }

        }

        return true;
    }

    /**
     * @see ParserConfiguration#setNonGreedyMode(boolean)
     */
    private IStructureDefinition couldBeNotGreedy() {
    	for (int i = myCurrentDefinitionPath.size() - 1; i >= 1; i--) {
    		Position position = myCurrentDefinitionPath.get(i);
	    	IStructureDefinition curPos = position.getStructureDefinition();
	    	if (curPos.getPosition() > 0) {
	    		IStructureDefinition parent = curPos.getParent();
				if (parent.isRepeating() && parent.getAllPossibleFirstChildren().contains(myDirection)) {
	    			return parent;
	    		}
	    	}
	    	
    	}
    	
		return null;
	}

	private void addNonStandardSegmentAtCurrentPosition() throws Error {
    	log.debug("Creating non standard segment {} on group: {}", 
    			myDirection, getCurrentPosition().getStructureDefinition().getParent().getName());
        
    	List<Position> parentDefinitionPath;
        Group parentStructure;
        
        switch (myMessage.getParser().getParserConfiguration().getUnexpectedSegmentBehaviour()) {
        case ADD_INLINE:
        default:
        	parentDefinitionPath = new ArrayList<Position>(myCurrentDefinitionPath.subList(0, myCurrentDefinitionPath.size() - 1));
        	parentStructure = (Group) navigateToStructure(parentDefinitionPath);
        	break;
        case DROP_TO_ROOT:
        	parentDefinitionPath = new ArrayList<Position>(myCurrentDefinitionPath.subList(0, 1));
        	parentStructure = myMessage;
        	myCurrentDefinitionPath = myCurrentDefinitionPath.subList(0, 2);
        	break;
        case THROW_HL7_EXCEPTION:
        	throw new Error(new HL7Exception("Found unknown segment: " + myDirection));
        }
        
        
        // Current position within parent
        Position currentPosition = getCurrentPosition();
		String nameAsItAppearsInParent = currentPosition.getStructureDefinition().getNameAsItAppearsInParent();

		int index = Arrays.asList(parentStructure.getNames()).indexOf(nameAsItAppearsInParent) + 1;
		
        String newSegmentName;
		
		// Check if the structure already has a non-standard segment in the appropriate
		// position
		String[] currentNames = parentStructure.getNames();
		if (index < currentNames.length && currentNames[index].startsWith(myDirection)) {
			newSegmentName = currentNames[index];
		} else { 
	        try {
	            newSegmentName = parentStructure.addNonstandardSegment(myDirection, index);
	        } catch (HL7Exception e) {
	            throw new Error("Unable to add nonstandard segment " + myDirection + ": ", e);
	        }
	    }
		
        IStructureDefinition previousSibling = getCurrentPosition().getStructureDefinition();
        IStructureDefinition parentStructureDefinition = parentDefinitionPath.get(parentDefinitionPath.size() - 1).getStructureDefinition();
        NonStandardStructureDefinition nextDefinition = new NonStandardStructureDefinition(parentStructureDefinition, previousSibling, newSegmentName, index);
        myCurrentDefinitionPath = parentDefinitionPath;
        myCurrentDefinitionPath.add(new Position(nextDefinition, 0));

        myNextIsSet = true;
    }

    /**
     * <p>
     * Returns the next node in the message. Sometimes the next node is
     * ambiguous. For example at the end of a repeating group, the next node may
     * be the first segment in the next repetition of the group, or the next
     * sibling, or an undeclared segment locally added to the group's end. Cases
     * like this are disambiguated using getDirection(), which returns the name
     * of the structure that we are "iterating towards". Usually we are
     * "iterating towards" a segment of a certain name because we have a segment
     * string that we would like to parse into that node. Here are the rules:
     * </p>
     * <ol>
     * <li>If at a group, next means first child.</li>
     * <li>If at a non-repeating segment, next means next "position"</li>
     * <li>If at a repeating segment: if segment name matches direction then
     * next means next rep, otherwise next means next "position".</li>
     * <li>If at a segment within a group (not at the end of the group), next
     * "position" means next sibling</li>
     * <li>If at the end of a group: If name of group or any of its "first
     * decendents" matches direction, then next position means next rep of
     * group. Otherwise if direction matches name of next sibling of the group,
     * or any of its first descendents, next position means next sibling of the
     * group. Otherwise, next means a new segment added to the group (with a
     * name that matches "direction").</li>
     * <li>"First descendents" means first child, or first child of the first
     * child, or first child of the first child of the first child, etc.</li>
     * </ol>
     */
    public Structure next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more nodes in message");
        }

        Structure currentStructure = navigateToStructure(myCurrentDefinitionPath);

        clearNext();
        return currentStructure;
    }

    private Structure navigateToStructure(List<Position> theDefinitionPath) throws Error {
        Structure currentStructure = null;
        for (Position next : theDefinitionPath) {
            if (currentStructure == null) {
                currentStructure = myMessage;
            } else {
                try {
                    IStructureDefinition structureDefinition = next.getStructureDefinition();
                    Group currentStructureGroup = (Group) currentStructure;
                    String nextStructureName = structureDefinition.getNameAsItAppearsInParent();
                    currentStructure = currentStructureGroup.get(nextStructureName, next.getRepNumber());
                } catch (HL7Exception e) {
                    throw new Error("Failed to retrieve structure: ", e);
                }
            }
        }
        return currentStructure;
    }

    /** Not supported */
    public void remove() {
        throw new UnsupportedOperationException("Can't remove a node from a message");
    }

    public String getDirection() {
        return this.myDirection;
    }

    public void setDirection(String direction) {
        clearNext();
        this.myDirection = direction;
    }

    private void clearNext() {
        myNextIsSet = false;
    }

    /**
     * A structure position within a message.
     */
    public static class Position {
        private IStructureDefinition myStructureDefinition;
        private int myRepNumber = -1;

        public IStructureDefinition getStructureDefinition() {
            return myStructureDefinition;
        }

        public void resetRepNumber() {
            myRepNumber = -1;
        }

        public void setStructureDefinition(IStructureDefinition theStructureDefinition) {
            myStructureDefinition = theStructureDefinition;
        }

        public int getRepNumber() {
            return myRepNumber;
        }

        public Position(IStructureDefinition theStructureDefinition, int theRepNumber) {
            myStructureDefinition = theStructureDefinition;
            myRepNumber = theRepNumber;
        }

        public void incrementRep() {
            myRepNumber++;
        }

        /** @see Object#equals */
        public boolean equals(Object o) {
            boolean equals = false;
            if (o != null && o instanceof Position) {
                Position p = (Position) o;
                if (p.myStructureDefinition.equals(myStructureDefinition) && p.myRepNumber == myRepNumber)
                    equals = true;
            }
            return equals;
        }

        /** @see Object#hashCode */
        public int hashCode() {
            return myStructureDefinition.hashCode() + myRepNumber;
        }

        public String toString() {
            StringBuilder ret = new StringBuilder();

            if (myStructureDefinition.getParent() != null) {
                ret.append(myStructureDefinition.getParent().getName());
            } else {
                ret.append("Root");
            }

            ret.append(":");
            ret.append(myStructureDefinition.getName());
            ret.append("(");
            ret.append(myRepNumber);
            ret.append(")");
            return ret.toString();
        }
    }

    /**
     * Must be called after {@link #next()}
     */
    public int getNextIndexWithinParent() {
        return getCurrentPosition().getStructureDefinition().getPosition();
    }
}
