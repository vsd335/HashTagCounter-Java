/**
 * 							PROBLEM STATEMENT
 * You are required to implement a system to find the n most popular hashtags 
 * appeared on social media such as Facebook or Twitter. For the scope of this 
 * project hashtags will be given from an input file. 

 * Basic idea for the implementation is to use a max priority structure to find 
 * out the most popular hashtags.

 * You must use following structures for the implementation.
 * 1. Max Fibonacci heap: use to keep track of the frequencies of hashtags.
 * 2. Hash table: Key for the hash table is hashtag and value is pointer to the 
 * corresponding node in the Fibonacci heap.

 * You can assume there will be a large number of hashtags appears in the stream 
 * and you need to perform increase key operation many times. Max Fibonacci heap 
 * is required because it has better theoretical bounds for increase key operation
 **/
 
/** Author : Vinayak Deshpande
  * UFID   : 4102 9538
  **/ 	


import java.io.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map.Entry;
    
public class hashtagcounter {
	
	
   /**
     * This inner class holds the node structure for the Fibonacci heap with private fields
    **/
    public static final class HeapEntry {
     
		private int hDegree = 0;	 			// Number of children in the node
		 
		public HeapEntry hLeftSib;  			// Left Sibling element 
		public HeapEntry hRightSib; 			// Right Sibling element
		
		public HeapEntry hParent= null;			// Parent in the fibonacci heap (if any)
		public HeapEntry hChild = null;    		// Child node (if any)
	    
		public int	hElem;		 				// Element stored in the node
		public boolean hChildCut = false;	 	// Whether a node has lost a child or not
		
		public String hHashTag;					// Holds the hash tag
		
	    // Set the initial left and right sibling of a new node to itself
	    public HeapEntry(int elem, String hHashTag) {
	    	hRightSib = hLeftSib = this;
	    	this.hHashTag = hHashTag;
	    	hElem = elem;	    	
        }
	    
    } 
	
    // Hash Map for all the elements to be inserted into the heap
    static HashMap<String, HeapEntry> hm = new HashMap<>();
    
    /** Hash Map for all the elements to be inserted into the heap **/
    HashMap<Integer, HeapEntry> degreeMap = new HashMap<>();
    
    /** Pointer to the maximum element in the heap. */
    public HeapEntry hMax = null;
    
    /** Store the size of the heap **/
    public int hSize = 0;

    /** Insert() : Insert a new node into the heap **/
    public void InsertNode(HeapEntry newNode) {
    	
    		   	
    		if(hMax == null)
    		{
    			 hMax = newNode;
    			 hMax.hParent=null;
    			 hMax.hLeftSib = hMax;
    			 hMax.hRightSib = hMax;
    			 hMax.hChildCut = false;
    			 
    		}
    		else {
    		
    			hMax = InsertIntoRootList(hMax, newNode);
    		}										
    		hSize += 1;						// Increase the size of the heap, as we just inserted a new node
    		
    		// Always ensure that, hMax is pointing to the appropriate max node in the heap
    		HeapEntry maxNodeTemp = hMax.hLeftSib;
    		HeapEntry currNode = hMax;
			do
			{
				if(currNode.hRightSib.hElem > hMax.hElem)
				{
					
					hMax = currNode.hRightSib;
					
				}
				currNode= currNode.hRightSib;
			}while(currNode != maxNodeTemp);
    		
    		return;
    }
    
    /** IncreaseKey() : Increase the value of a node in the heap **/
    public void IncreaseKey(HeapEntry node, int newVal) {
    		
    		node.hElem = node.hElem + newVal;				// Change the value at the node to the new larger value
    		
    		HeapEntry cacheParent = node.hParent;			// The Parent of the node is stored
    		    
    		// Check to see if the child node's count value is greater than its parent
    		if(cacheParent != null && (Integer)node.hElem > (Integer) cacheParent.hElem) {
    			
    				// Cut() & CascadeCut() : Cut the node from its parent node and cascade into root node
    				NodeCut(node, cacheParent);
    				NodeCascade(cacheParent);
    			
    		}
    		
    		if((Integer)node.hElem > (Integer)hMax.hElem) {
    			
    			hMax = node;								// New node with larger count value becomes hMax
    		}
    }
        
    /** RemoveMax() : Remove the max node from the heap **/
    public HeapEntry RemoveMax() {
    		
    		HeapEntry cacheMaxNode = hMax;					// Need to return this to calling function    	
    	    	
    		if(hMax.hRightSib == hMax) {					// For A single node at root
								
    			hMax = null;								// Set the max pointer to null & the heap becomes empty
    			AddChildren2Root(cacheMaxNode);
    		
    		}
    		else {											// For more than one node at root
    		
    			// Max Nodes Siblings must be joined
    			hMax.hRightSib.hLeftSib = hMax.hLeftSib;
    			hMax.hLeftSib.hRightSib = hMax.hRightSib;
    		
    			HeapEntry rightChild = cacheMaxNode.hRightSib;
    		    		
    			hMax = null;								// Set the max pointer to null, may be not required (CHECK later)
    			hMax = rightChild;
    		
    			AddChildren2Root(cacheMaxNode);				// Insert the children of Max node into the root of the heap
    			
    			HeapEntry currPointer = hMax;
    		
    			// This loop is to find out the new max node
    			do {
    				if(currPointer.hRightSib.hElem > hMax.hElem) {
    				
    					hMax = currPointer.hRightSib;
    				}
    			
    				currPointer = currPointer.hRightSib;
    			}while(currPointer != rightChild);
    		    		
    		}
   	
    		/** Pairwise combine starts here **/       
    		/** Need to keep track of degree of nodes to combine in pairs**/
    		degreeMap = new HashMap<Integer, HeapEntry>();
        	
          	/** This function will combine nodes with same degree until no two nodes in the root have same degree **/
    		RecursiveMerge(hMax);
                
    		hSize -= 1;		// Decrease the size of the heap, as we just removed a node
	        cacheMaxNode.hLeftSib = cacheMaxNode.hRightSib = cacheMaxNode;             
	        cacheMaxNode.hParent = null;
	        cacheMaxNode.hChild = null;
	        cacheMaxNode.hDegree = 0;
        
	        // Return the max node to main function to write to the output file
           	return cacheMaxNode;
    }    
    
    /** This method adds the children of a removed node to root node list of the heap **/
    public void AddChildren2Root(HeapEntry firstChild) {
    	
	    	HeapEntry tempNode = firstChild.hChild;
	    	HeapEntry sibling;
	    	
	    	if (firstChild.hDegree == 0) {					// Max node has no children
	    		
				return;				
	    	}    	
	    	else if (firstChild.hDegree == 1) { 		    // Max node has only one child
	    						
				InsertNode(tempNode);						// Insert the only child to root of heap			
				
	    	}    	
	    	else { 											// Max node has more than one child
			    			   		
				for(int k=0; k<firstChild.hDegree; k++) {
				  
				    HeapEntry currChild = firstChild.hChild;
				    sibling = currChild.hRightSib;
				    firstChild.hChild = sibling;
				    currChild.hRightSib = currChild.hLeftSib = currChild;
				    InsertNode(currChild);
				}
			
	    	}
    	
	    	firstChild.hChild = null;
	    	return;
    }
    
    /** This method recursively combines nodes until no two nodes have same degree **/
    public void RecursiveMerge(HeapEntry pairNode1) {
    	    	
		do
		{
			int degree = pairNode1.hDegree;			// Degree of the node 
			
			if(degreeMap.containsKey(degree))		// Same degree node is present in hash map
			{

				if(pairNode1 != degreeMap.get(degree))
				{
					HeapEntry pairNode2 = degreeMap.remove(degree);
					
					// Returns the parent node (Node with larger count value)
					HeapEntry parentNode = CombineThePairs(pairNode1, pairNode2);
					
					pairNode1 = parentNode;					
					RecursiveMerge(pairNode1);
					return;		
				}
			}
			
			else
			{
				
				degreeMap.put(degree, pairNode1);	// Update degree table with this degree and node
			}
			pairNode1 = pairNode1.hRightSib;
		}while(pairNode1 !=hMax);
		
		return;
    		  
    }
    

    /** This method combines two nodes such that, one of the nodes becomes child of another node **/
    public HeapEntry CombineThePairs(HeapEntry pairNode1, HeapEntry pairNode2) { 
    
    		HeapEntry parentNode, childNode;
				
    		// This is to handle similar count hashTags, need to make sure, the max node always stays at the root
			if(pairNode1 == hMax || pairNode2 == hMax) {
				
				if(pairNode1 == hMax)
				{
					pairNode2.hLeftSib.hRightSib =  pairNode2.hRightSib;
					pairNode2.hRightSib.hLeftSib =  pairNode2.hLeftSib;
					
					pairNode2.hRightSib = pairNode2.hLeftSib = pairNode2;
					
					parentNode = pairNode1;
					childNode  = pairNode2;
				}
				
				else
				{
					pairNode1.hLeftSib.hRightSib =  pairNode1.hRightSib;
					pairNode1.hRightSib.hLeftSib =  pairNode1.hLeftSib;
					
					pairNode1.hRightSib = pairNode1.hLeftSib = pairNode1;
					
					parentNode = pairNode2;
					childNode  = pairNode1;
				}
				
			}
			
			else if(pairNode1.hElem > pairNode2.hElem) {				// Need to remove pairNode2 from root
				
				pairNode2.hLeftSib.hRightSib =  pairNode2.hRightSib;
				pairNode2.hRightSib.hLeftSib =  pairNode2.hLeftSib;
				
				pairNode2.hRightSib = pairNode2.hLeftSib = pairNode2;
											
				parentNode = pairNode1;
				childNode  = pairNode2;
			}
			else {									// Need to remove pairNode2 from root
		
				pairNode1.hLeftSib.hRightSib =  pairNode1.hRightSib;
				pairNode1.hRightSib.hLeftSib =  pairNode1.hLeftSib;
		
				pairNode1.hRightSib = pairNode1.hLeftSib = pairNode1;
		
				parentNode = pairNode2;
				childNode  = pairNode1;
	
			}
		
	    	if(parentNode.hDegree == 0) {								// If the parent has no previous children, Insert new child
	    		
	    		parentNode.hChild = childNode;
	    		childNode.hParent = parentNode;
	    		childNode.hRightSib = childNode.hLeftSib = childNode;
	    	}
	    	else {

	    		childNode.hRightSib = childNode.hLeftSib = childNode;
	    		HeapEntry defaultChild = parentNode.hChild;	 	         // Accessing the already present child node
	    		HeapEntry valNext = defaultChild.hRightSib; 	 		 // Store this since we are going to overwrite it.
	    		defaultChild.hRightSib = childNode;
	    		defaultChild.hRightSib.hLeftSib = defaultChild;
	    		childNode.hRightSib = valNext;
	    		childNode.hRightSib.hLeftSib = childNode;
	    		parentNode.hChild = childNode;
	    		childNode.hParent = parentNode;
			
	    	} 

	    	parentNode.hDegree++;										 // Increase the degree of parent node as we just inserted a new child
	     	return parentNode;
    }
    
    
    /** The main method reads the input file containing hashTags and calls various methods and finally writes to the output file **/
    public static void main(String [] args) {

        // The Input File containing hash tags.
        String inFile = args[0];
        
        // The Output File containing top most n hash tags 
        String outFile = "output_file.tx";

        // This will read one hash tag at a time
        String readHashTag = null;        
               
               
        try {
            
        	// FileReader reads the input text file in the default encoding.
            FileReader inFileReader = new FileReader(inFile);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(inFileReader);
            
            // FileWriter writes the output text file.
            FileWriter outFileWriter = new FileWriter(outFile);
        	
            // Always wrap FileReader in BufferedReader.
            BufferedWriter bufferedWriter = new BufferedWriter(outFileWriter);

            // hashtagcounter class object to access methods
    	    hashtagcounter hashObject = new hashtagcounter();
            
    	    hm = new HashMap<String, HeapEntry>();
    	    
            // Reading the hash tags into hashTag
            while((readHashTag = bufferedReader.readLine()) != null) {
            	
            	// To Separate hash tags entries from queries
            	char first = readHashTag.charAt(0);            	
            	if(first == '#') {							// These are hash tags
            		
            		String hashTag;
            		int hashTagCount;
            		
            		String[] split = readHashTag.trim().split("\\s+");
            		
            		hashTag = split[0];
            		hashTag = hashTag.substring(1,  hashTag.length());
            		hashTagCount = Integer.parseInt(split[1]);
            		                   		
            		if(hm.containsKey(hashTag)) {
            			
            			// hashTag is present in HashMap so do IncreaseKey
            			HeapEntry node1 = hm.get(hashTag);
            			hashObject.IncreaseKey(node1, hashTagCount);
            		
            		}
            		else {
            			
            			// hashTag is not present in HashMap so do Insert
            			HeapEntry node2 = new HeapEntry(hashTagCount, hashTag);
            			hm.put(hashTag, node2);
            			hashObject.InsertNode(node2);
            			
            		}
            	}
                       	
            	else if (readHashTag.equals("stop") || readHashTag.equals("STOP")) {	// Stop here
            		
            		bufferedReader.close();     
            		bufferedWriter.close();
        			System.exit(0);
            		
            	}
            	
            	else {									// These are removeMax queries 
            		
            		int query = Integer.parseInt(readHashTag); 

            		try {
            			
            			// Hash Map for all the removed elements to be inserted back into the heap
            			HashMap<String, Integer> dict = new HashMap<>();
				
            			if(query > hashObject.hSize) {
            				//query = hSize;
				  
            			}
				
            			// Remove Max and Pairwise Combine operations
            			for(int j=0 ; j<query; j++) {
            				
            				HeapEntry node3 = hashObject.RemoveMax();       			
            				String key = node3.hHashTag;            				
            				dict.put(key, node3.hElem);
            						
					// Write to Output file here
            				bufferedWriter.write(key);
            						
            	                   	if(j<query-1)
	                        		bufferedWriter.write(',');
            			}       
            			
            			// Insert the above removed nodes back into the root of Fibonacci heap
            			for(Entry<String, Integer> entry : dict.entrySet()) {
						
        					String key = entry.getKey();
        					Integer value = entry.getValue();
        					HeapEntry node5 = new HeapEntry(value, key); 
        					hashObject.InsertNode(node5);
        					hm.put(key, node5);
        				}
            			            			
            			bufferedWriter.write('\n');
            		}
            		
            		catch(FileNotFoundException ex) {
                        System.out.println("Unable to open file '" + outFile + "'");                
                    }
                    catch(IOException ex) {
                        System.out.println("Error writing file '" + outFile + "'"); 
                    }
            	}
            }   

            // Always close files.
            bufferedReader.close(); 
            bufferedWriter.close();            
                       
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + inFile + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + inFile + "'");                  
        }
        
    } 
    
    
    /** Merge new node with the List of nodes already present in the heap**/
    /** val1 = current max node, val2 = new node **/
    public static HeapEntry InsertIntoRootList(HeapEntry oldNode, HeapEntry newNode) {
    	    	    	
    	
    	
    	if (oldNode == null && newNode == null) {     // Both val1 & val2 are null & the resulting list is null.
    		return null;
        }
    	
    	else if (oldNode != null && newNode == null) { // val2 is null, result is val1.
            return oldNode;
        }
    	
    	else if (oldNode == null && newNode != null) { // val1 is null, result is val2.
    		newNode.hParent=null;
			newNode.hChildCut = false;
			if(newNode.hDegree==0)
				newNode.hChild=null;
            return newNode;
        }
    	
    	else {
    		
    		HeapEntry valNext = oldNode.hRightSib; // Store this since we are going to overwrite it.
            oldNode.hRightSib = newNode.hRightSib;
            oldNode.hRightSib.hLeftSib = oldNode;
            newNode.hRightSib = valNext;
            newNode.hRightSib.hLeftSib = newNode;  
            newNode.hParent=null;
			newNode.hChildCut = false;
			if(newNode.hDegree==0)
				newNode.hChild=null;
    		
            /* Return a pointer to the Larger node */
            return (Integer)oldNode.hElem > (Integer)newNode.hElem? oldNode : newNode; 
    	}
    	
    }
    
    /** Separates the Child node from its parent node, as the key of Child is larger than its Parent **/
    public void NodeCut(HeapEntry childNode, HeapEntry parentNode) { 
    	
    	// Set the parent of the node to removed to null
    	childNode.hParent = null;
    	
    	// When the childNode has siblings, set right sibling as new child of parent
    	if(parentNode.hChild == childNode && childNode.hRightSib != childNode) { 

    		parentNode.hChild = childNode.hRightSib;
    		childNode.hLeftSib.hRightSib = childNode.hRightSib;
    		childNode.hRightSib.hLeftSib = childNode.hLeftSib;
    		
    	}
    	// When the childNode has siblings, and is not the registered child of parentNode
    	else if (parentNode.hChild != childNode && childNode.hRightSib != childNode){
    		
    		childNode.hLeftSib.hRightSib = childNode.hRightSib;
    		childNode.hRightSib.hLeftSib = childNode.hLeftSib;
    		
    	}
    	else {
    		
    		parentNode.hChild = null;
    	
    	}
    	
    	childNode.hRightSib = childNode.hLeftSib = childNode;
    	childNode.hChildCut = false;	// ChildCut is set to false    	
    	parentNode.hDegree--;			// Decrease the degree of the parent node
    	InsertNode(childNode);			// Remove the node and insert into the root list
    	
    	return;
    	
    }
    
    /** Inserts the Separated Child into the root node List **/
	public void NodeCascade(HeapEntry parentNode) { 
    		
		HeapEntry grandParentNode = parentNode.hParent;		// Store the parent of parentNode
		
    	if(grandParentNode != null) {
    		
    		if((boolean)parentNode.hChildCut == false) {	// If the node has not lost a child before
    			
    			parentNode.hChildCut = true; 				// Set its childCut to true
    			
    		}
    		else {

    			// Cut() & CascadeCut() : Cut the node from its parent node and cascade into root node
				NodeCut(parentNode, grandParentNode);
				NodeCascade(grandParentNode);
    			
    		}	
    	}
    	
    	return;
    }
}
