package com.dbf.utils.stacktrace;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class StackTraceCompactor {
	
	/***
	 * Returns a compacted stack trace from a provided Throwable.
	 * 
	 * @param t - the Throwable
	 * @return a compacted stack trace as a String
	 */
	public static String getCompactStackTrace(Throwable t) {
		StringBuilder sb = new StringBuilder(200);
		//Apache Commons produces a more compact stack trace that clearly indicates [wrapped] lines
		String[] stackElements = ExceptionUtils.getRootCauseStackTrace(t);
		collapseStackElements(stackElements);
		for (final String element : stackElements) {
			sb.append(element);
			sb.append(System.lineSeparator());
		}
		return sb.toString();		
	}
	
	/***
	 * Makes an array of stack trace elements more compact by collapsing previously
	 * seen package names and removing redundant information.
	 * 
	 * @param stackElements - an array of stack trace elements
	 * @return void
	 */
	public static void collapseStackElements(String[] stackElements) {
		if (stackElements.length < 2) return;
		
		String previousValidPackageName = "";
		String previousExceptionMessage = stackElements[0];
		
		StringBuilder sb = new StringBuilder(200); //Reuse the string builder
		for(int i = 1; i < stackElements.length; i++) {
			sb.setLength(0);
			String line = stackElements[i];
				
			//Validate - Check that the line starts looks like a regular stack trace line
			//eg:    at okhttp3.internal.connection.RealConnection.connect(RealConnection.java:149)
			int atIndex = line.indexOf("at ");
			if(atIndex > -1) {
				int openBracketIndex = line.lastIndexOf('(');
				if(openBracketIndex < 0) continue;
				
				int methodIndex = line.lastIndexOf('.', openBracketIndex);
				if(methodIndex < 0) continue;
				
				int closeBracketIndex = line.lastIndexOf(')');
				if(closeBracketIndex < 0 || closeBracketIndex <= openBracketIndex) continue;
				
				//Extract the package name
				String packageName = line.substring(atIndex + 3, methodIndex);
				
				//Determine the class name
				//Note: could also be in the form of 'at RealConnection.connect(RealConnection.java:149)'
				int classIndex = line.lastIndexOf('.', methodIndex - 1);
				String className = (classIndex < 0) ? packageName : line.substring(classIndex + 1, methodIndex);
				
				//ClassName might contain an inner class
				//eg: at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
				int innerClassIndex = className.indexOf("$");
				if (innerClassIndex > 0) className = className.substring(0, innerClassIndex);
				
				//Determine the file name
				//Note: could also be in the form of 'at okhttp3.internal.connection.RealConnection.connect(RealConnection.java)'
				//or 'at okhttp3.internal.connection.RealConnection.connect(RealConnection)'
				//or 'at java.net.PlainSocketImpl.socketConnect(Native Method)'
				int colonIndex = line.indexOf(':', openBracketIndex);
				int dotIndex = line.indexOf('.', openBracketIndex);
				int fileEndIndex = (colonIndex < 0) ? closeBracketIndex : colonIndex;
				String fileName = line.substring(openBracketIndex + 1, (dotIndex < 0) ? fileEndIndex : dotIndex);
				
				//Build the new line
				sb.append(line.substring(0, atIndex + 3)); //"    at "
				
				//Determine package overlap with the previous package name
				sb.append(overlapPackageName(previousValidPackageName, packageName));
			
				sb.append(line.substring(methodIndex, openBracketIndex + 1));
				
				if(fileName.equals(className)) {
					//Check if it Follows standard java convention for naming ("ParentRunner.java")
					if(dotIndex > -1 && !line.substring(openBracketIndex + 1, fileEndIndex).endsWith(".java")) {
						//Replace just the first part and keep the extension
						//eg. "(ParentRunner.crazy:56)" -> "(~.crazy:56)"
						sb.append("~");
						sb.append(line.substring(dotIndex, closeBracketIndex + 1));
					} else {
						//Retain just the line number, if present. Retain nothing otherwise.
						if(colonIndex > 0) {
							//"(ParentRunner.java:56)" -> "(56)"
							sb.append(line.substring(colonIndex +1 , closeBracketIndex + 1));
						}else {
							//eg. "(ParentRunner.java)" -> "()"
							sb.append(")");
						}
					}
				} else {
					//Keep everything in the bracket as-is
					//eg. (ParentRunner.java:66)
					sb.append(line.substring(openBracketIndex + 1, closeBracketIndex + 1));
				}
						
				stackElements[i] = sb.toString();
				previousValidPackageName = packageName;
			} else {
				//This is likely a "wrapped" line
				//eg:    [wrapped] java.lang.RuntimeException: java.lang.IllegalArgumentException: Bad file provided.
				int wrappedIndex = line.indexOf("[wrapped] ");
				if(wrappedIndex < 0) continue;
				
				stackElements[i] = line.replace(previousExceptionMessage, "").trim(); //This will intentionally remove the leading space
				previousExceptionMessage = line.substring(wrappedIndex + "[wrapped] ".length());
			};
		}
	}
	
	private static String overlapPackageName(String basePackageName, String overlapPackageName) {
		return overlapPackageName(basePackageName, overlapPackageName, '.');
	}
	private static String overlapPackageName(String basePackageName, String overlapPackageName, char packageSeperator) {
		if(basePackageName.isEmpty())
    		return overlapPackageName;
		
		String[] baseSplits = StringUtils.split(basePackageName, packageSeperator);
		String[] overlapSplits = StringUtils.split(overlapPackageName, packageSeperator);
		
		StringBuilder sb = new StringBuilder(overlapPackageName.length());
		String seperator = "";
		boolean matchComplete = false;
		
		for(int i = 0; i < overlapSplits.length; i++) {
			final String segment = overlapSplits[i];
			sb.append(seperator);
			if(matchComplete || i > baseSplits.length -1 ) {
				//If this is an extra segment, or matching has completed, then append it as-is
				sb.append(segment);
				matchComplete = true;
			} else if (!segment.equals(baseSplits[i])) { 
				//The segments don't match, check for inner class
				//eg. "ParentRunner$2" vs. "ParentRunner$4" vs. "ParentRunner"
				if(segment.indexOf('$') > 0 || baseSplits[i].indexOf('$') > 0) {
					final String newSegment = overlapPackageName(baseSplits[i], segment, '$');
					sb.append(newSegment);
					matchComplete = segment.equals(newSegment);
				} else {
					sb.append(segment);
					matchComplete = true;
				}
			}else {
				//This segment is the same. Condense it
				sb.append(segment.charAt(0));
			}
			seperator = "" + packageSeperator;
		}
		return sb.toString();
	}
}
