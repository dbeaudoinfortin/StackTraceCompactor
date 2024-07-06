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
		
		sb.append("\nStackTrace:\n");
		
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
		String previousValidPackageName = "";
		StringBuilder sb = new StringBuilder(200); //Reuse the string builder
		
		for(int i = 0; i < stackElements.length; i++) {
			sb.setLength(0);
			String line = stackElements[i];
				
			//Validate - Check that the line starts looks like a regular stack trace line
			//eg:    at okhttp3.internal.connection.RealConnection.connect(RealConnection.java:149)
			int atIndex = line.indexOf("at ");
			if(atIndex < 0) continue;
			
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
			
			//Determine the file name
			//Note: could also be in the form of 'at okhttp3.internal.connection.RealConnection.connect(RealConnection.java)'
			//or 'at okhttp3.internal.connection.RealConnection.connect(RealConnection)'
			//or 'at java.net.PlainSocketImpl.socketConnect(Native Method)'
			int colonIndex = line.indexOf(':', openBracketIndex);
			int dotIndex = line.indexOf('.', openBracketIndex);
			int fileEndIndex = (colonIndex < 0) ? closeBracketIndex : colonIndex;
			String fileName = line.substring(openBracketIndex + 1, (dotIndex < 0) ? fileEndIndex : dotIndex);
			String fileNameWithExtension = line.substring(openBracketIndex + 1, fileEndIndex);
			
			//Build the new line
			sb.append(line.substring(0, atIndex + 3)); //"    at "
			
			//Determine package overlap with the previous package name
			sb.append(overlapPackageName(previousValidPackageName, packageName));
		
			sb.append(line.substring(methodIndex, openBracketIndex + 1));
			
			if(fileName.equals(className)) {
				sb.append("~");
				//Check if it Follows standard java convention for naming
				if(dotIndex > -1 && !fileNameWithExtension.endsWith(".java")) {
					sb.append(line.substring(dotIndex, fileEndIndex));
				}
			} else {
				sb.append(fileNameWithExtension);
			}
			sb.append(line.substring(fileEndIndex, closeBracketIndex + 1));
					
			stackElements[i] = sb.toString();
			previousValidPackageName = packageName;
		}
	}
	
	private static String overlapPackageName(String basePackageName, String overlapPackageName) {
		if(basePackageName.isEmpty())
    		return overlapPackageName;
		
		String[] baseSplits = StringUtils.split(basePackageName, '.');
		String[] overlapSplits = StringUtils.split(overlapPackageName, '.');
		
		StringBuilder sb = new StringBuilder(overlapPackageName.length());
		String seperator = "";
		boolean matchComplete = false;
		
		for(int i = 0; i < overlapSplits.length; i++) {
			String segment = overlapSplits[i];
			sb.append(seperator);
			if(matchComplete || i > baseSplits.length -1 || !segment.equals(baseSplits[i])) {
				//If this is an extra segment, or the segments don't match, or matching has completed, then append it as-is
				sb.append(segment);
				matchComplete = true;
			} else {
				//This segment is the same. Condense it
				sb.append(segment.charAt(0));
			}
			seperator = ".";
		}
		return sb.toString();
	}
}
