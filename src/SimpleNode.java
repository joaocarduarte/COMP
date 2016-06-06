import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import graphviz.GraphViz;

/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.3 */
/* JavaCCOptions:MULTI=false,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class SimpleNode implements Node {

	protected Node parent;
	protected Node[] children;
	protected int id; 
	protected Object value;
	protected Object name;
	protected Object content;
	protected Object code;
	protected ExampleParser parser;
	protected  static Boolean foundmain = false;
	HashMap<String, ArrayList<String>> variables;


	public SimpleNode(int i) {
		id = i;
	}

	public SimpleNode(ExampleParser p, int i) {
		this(i);
		parser = p;
	}

	public void jjtOpen() {
	}

	public void jjtClose() {
	}

	public void jjtSetParent(Node n) { parent = n; }
	public Node jjtGetParent() { return parent; }

	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		} else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild(int i) {
		return children[i];
	}

	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}

	public void jjtSetValue(Object value) { this.value = value; }
	public Object jjtGetValue() { return value; }

	/* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

	public String toString() { return ExampleParserTreeConstants.jjtNodeName[id]; }
	public String toString(String prefix) { return prefix + toString(); }

	/* Override this method if you want to customize how the node dumps
     out its children. */

	public void dump(String prefix) {
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				SimpleNode n = (SimpleNode)children[i];
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}
	
	public void printstuff(String prefix){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("dotfile.dot", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		writer.println("digraph graphname{");
		ArrayList<String> order = new ArrayList<String>();
		filter(writer,prefix, order);
		writer.println("}");
		writer.close();
	}

	private void addToVariables(String variable, String path, HashMap<String, ArrayList<String>> definedVariables)
	{
		if(definedVariables.containsKey(variable))
		{
			if(definedVariables.get(variable).size() > 1)
			{
				definedVariables.get(variable).remove(1);
				definedVariables.get(variable).add(path);
			}
		}
		else
		{
			definedVariables.put(variable, new ArrayList<String>());
			definedVariables.get(variable).add(path);
			definedVariables.get(variable).add(path);
		}
	}

	public void filter(PrintWriter writer,String string, ArrayList<String> order) {
		// TODO Auto-generated method stub
		variables = new HashMap<String, ArrayList<String>>();
		if (this.children != null) {
			for (int i = 0; i < this.children.length; ++i) {
				SimpleNode n = (SimpleNode)this.children[i];
				if(n.name!=null){
					if(n.name.equals("\"Method\"")){
						if(n.content.equals("\"main\"")){ //para o caso do main
							SimpleNode c = (SimpleNode)n.children[0];
							foundmain = true;

							if(n.children.length == 2) //estamos a contar que exista sempre Block
								writer.println(")");
							else {
								String mainSentence = null;
								for (int j = 1; j < n.children.length - 1; j++) { 
									mainSentence = "\"" + removeQuotationMarks(c.content) + " " + removeQuotationMarks(n.content) +"(";
									c = (SimpleNode)n.children[j];
									SimpleNode cc = (SimpleNode)c.children[0];

									if(cc.name.equals("\"TypeReference\"")) //se for typereference nao tem mais filhos
									{
										mainSentence += removeQuotationMarks(cc.content) + " " + removeQuotationMarks(c.content);
										variables.put(removeQuotationMarks(cc.content), new ArrayList<String>());
									}

									if(cc.name.equals("\"ArrayTypeReference\"")){ //se for arraytypereference tem mais um filho
										SimpleNode ccc = (SimpleNode)cc.children[0];
										mainSentence += removeQuotationMarks(ccc.content) + "[] " + removeQuotationMarks(c.content);
										variables.put(removeQuotationMarks(cc.content), new ArrayList<String>());
									}

									if(j == n.children.length - 2){
										mainSentence += ")\"";
										order.add(mainSentence);
										writer.print(order.get(0));
										break;
									}
									else {
										mainSentence += ", ";
									}

								}
								for ( String key : variables.keySet() ) {
									variables.put(key, new ArrayList<String>());
									variables.get(key).add(mainSentence);
									variables.get(key).add(mainSentence);
								}
							}
							SimpleNode block = (SimpleNode)n.children[n.children.length-1];
							analyzeBlock(block, writer);
						}
					}
				}
				//System.out.println(n.name);
				if(n != null)
					//  writer.print("gotcha");

					n.filter(writer, string, order);
			}
		}

	}

	private String removeQuotationMarks(Object content2)
	{
		String operation1 = ((String) content2).substring(1, ((String) content2).length() - 1);
		return operation1;
	}

	private void analyzeBlock(SimpleNode block, PrintWriter writer) {
		HashMap<String, ArrayList<String>> definedVariables = new HashMap<String, ArrayList<String>>();
		for(int k = 0; k < block.children.length; k++)
		{
			SimpleNode c = (SimpleNode)block.children[k];

			////////////////////////////////////////// BEGIN IF
			if(c.name.equals("\"If\"") && k + 1 < block.children.length) 
			{
				analyzeIf(c, writer, (SimpleNode)block.children[k+1], definedVariables);
				writer.print(analyzeLine((SimpleNode)block.children[k+1], definedVariables));
				k++;
			}
			else if(c.name.equals("\"If\"")) 
			{
				analyzeIf(c, writer, definedVariables);
			}
			//////////////////////////////////////////END IF

			//////////////////////////////////////////Begin While
			else if(c.name.equals("\"While\"") && k + 1 < block.children.length)
			{
				analyzeWhile(c, writer, (SimpleNode)block.children[k+1], definedVariables);
				writer.print(analyzeLine((SimpleNode)block.children[k+1], definedVariables));
				k++;
			}
			else if(c.name.equals("\"While\""))
			{
				analyzeWhile(c, writer, definedVariables);
			}
			///////////////////////////////////////////END While

			//////////////////////////////////////////Begin For
			else if(c.name.equals("\"For\"") && k + 1 < block.children.length)
			{
				analyzeFor(c, writer, (SimpleNode)block.children[k+1], definedVariables);
				writer.print(analyzeLine((SimpleNode)block.children[k+1], definedVariables));
				k++;
			}
			else if(c.name.equals("\"For\""))
			{
				analyzeFor(c, writer, definedVariables);
			}
			//////////////////////////////////////////END For
			
			else{ 	//imprime tudo que nao e ifs, fors e whiles
				writer.print( " -> " +analyzeLine(c, definedVariables));
			}
		}
		for ( String key : definedVariables.keySet() )
		{
			writer.println(definedVariables.get(key).get(0) + " -> " + definedVariables.get(key).get(1) + "[label=\""+ key +"\" style=\"dotted\"]");
		}
	}

	// For SEM codigo a frente
	private void analyzeFor(SimpleNode c, PrintWriter writer, HashMap<String, ArrayList<String>> definedVariables) { 

		// TODO ver se after nao e ciclo ou if

		String forCondition = null;
		String firstCondition = null, secondCondition = null, thirdCondition = null; //FOR tem 3 condicoes
		SimpleNode cc = (SimpleNode)c.children[0];
		if(cc.name.equals("\"LocalVariable\""))	//FOR
		{
			//FIRST CONDITION
			SimpleNode ccc = (SimpleNode)cc.children[1];
			if(ccc.name.equals("\"Literal\""))
			{
				firstCondition = removeQuotationMarks(cc.content) + " = " + removeQuotationMarks(ccc.content);
			}

			//SECOND CONDTION
			cc = (SimpleNode)c.children[1];
			String binaryOperator = (String) cc.content;
			String compare1 = null;
			String compare2 = null;
			ccc = (SimpleNode)cc.children[1];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare1 = (String) cccc.content;
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare1 = (String) ccc.content;
			}

			ccc = (SimpleNode)cc.children[2];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare2 = (String) cccc.content;
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare2 = (String) ccc.content;
			}

			secondCondition = removeQuotationMarks(compare1) + removeQuotationMarks(binaryOperator) + removeQuotationMarks(compare2);
			//THIRD CONDITION
			cc = (SimpleNode)c.children[2];
			if(cc.name.equals("\"UnaryOperator\""))
			{
				String operator = (String) cc.content;
				ccc = (SimpleNode) cc.children[1];
				if(ccc.name.equals("\"VariableRead\""))
				{
					SimpleNode cccc = (SimpleNode) ccc.children[1];
					if(cccc.name.equals("\"LocalVariableReference\""))
					{
						thirdCondition = removeQuotationMarks(cccc.content) + (removeQuotationMarks(operator)).substring(1, removeQuotationMarks(operator).length());
					}
				}
			}

			forCondition = "\"For(" + firstCondition + ";" + secondCondition + ";" + thirdCondition + ")\"";	
			writer.println(" -> " + forCondition);
		}
		//CONDICIONADOS
		String lastLine = "";			//*ultima linha para ser adicionado ao arraylist*
		cc = (SimpleNode)c.children[3];			//CONDICIONADO TRUE
		writer.print(forCondition);
		if(cc.name.equals("\"Block\"")){	//caso seja um block
			for(int l = 0; l < cc.children.length; l++)		
			{
				SimpleNode ccc = (SimpleNode)cc.children[l];

				lastLine = analyzeLine(ccc, definedVariables);


				if(l == (cc.children.length - 1)){
					//lastConditioned.add(lastLine);
					writer.println(" -> " + lastLine);
					writer.println(lastLine + " -> " + forCondition);
				}
				else
					writer.print(" -> " + lastLine);
			}
		}
	}

	// For COM codigo a frente
	private void analyzeFor(SimpleNode c, PrintWriter writer, SimpleNode after, HashMap<String, ArrayList<String>> definedVariables) { 
		// TODO ver se after nao e ciclo ou if

		String forCondition = null;
		String firstCondition = null, secondCondition = null, thirdCondition = null; //FOR tem 3 condicoes
		ArrayList<String> variablesUsed = new ArrayList<String>();
		HashMap<String, ArrayList<String>> forVariablesUsed = new HashMap<String, ArrayList<String>>();
		SimpleNode cc = (SimpleNode)c.children[0];
		if(cc.name.equals("\"LocalVariable\""))	//FOR
		{
			//FIRST CONDITION
			SimpleNode ccc = (SimpleNode)cc.children[1];
			if(ccc.name.equals("\"Literal\""))
			{
				firstCondition = removeQuotationMarks(cc.content) + " = " + removeQuotationMarks(ccc.content);
			}

			//SECOND CONDTION
			cc = (SimpleNode)c.children[1];
			String binaryOperator = (String) cc.content;
			String compare1 = null;
			String compare2 = null;
			ccc = (SimpleNode)cc.children[1];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare1 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare1 = (String) ccc.content;
			}

			ccc = (SimpleNode)cc.children[2];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare2 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare2 = (String) ccc.content;
			}

			secondCondition = removeQuotationMarks(compare1) + removeQuotationMarks(binaryOperator) + removeQuotationMarks(compare2);
			//THIRD CONDITION
			cc = (SimpleNode)c.children[2];
			if(cc.name.equals("\"UnaryOperator\""))
			{
				String operator = (String) cc.content;
				ccc = (SimpleNode) cc.children[1];
				if(ccc.name.equals("\"VariableRead\""))
				{
					SimpleNode cccc = (SimpleNode) ccc.children[1];
					if(cccc.name.equals("\"LocalVariableReference\""))
					{
						if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
							variablesUsed.add(removeQuotationMarks(cccc.content));
						thirdCondition = removeQuotationMarks(cccc.content) + (removeQuotationMarks(operator)).substring(1, removeQuotationMarks(operator).length());
					}
				}
			}

			forCondition = "\"For(" + firstCondition + ";" + secondCondition + ";" + thirdCondition + ")\"";	
			for(int j = 0; j < variablesUsed.size(); j++)
			{
				addToVariables(variablesUsed.get(j), forCondition, forVariablesUsed);
			}
			writer.println(" -> " + forCondition);
		}
		//CONDICIONADOS
		String lastLine = "";			//*ultima linha para ser adicionado ao arraylist*
		cc = (SimpleNode)c.children[3];			//CONDICIONADO TRUE
		writer.print(forCondition);
		if(cc.name.equals("\"Block\"")){	//caso seja um block
			for(int l = 0; l < cc.children.length; l++)		
			{
				SimpleNode ccc = (SimpleNode)cc.children[l];

				lastLine = analyzeLine(ccc, definedVariables);


				if(l == (cc.children.length - 1)){
					//lastConditioned.add(lastLine);
					writer.println(" -> " + lastLine + " -> " + analyzeLine(after, definedVariables));
					writer.println(lastLine + " -> " + forCondition);
					for(int j = 0; j < variablesUsed.size(); j++)
					{
						addToVariables(variablesUsed.get(j), lastLine, forVariablesUsed);
						writer.println(forVariablesUsed.get(variablesUsed.get(j)).get(0) + " -> " + forVariablesUsed.get(variablesUsed.get(j)).get(1) + "[label=\""+ variablesUsed.get(j)+"\" style=\"dotted\"]");
					}
				}
				else
					writer.print(" -> " + lastLine);
			}
		}
	}
	
	// While SEM codigo a frente
	private void analyzeWhile(SimpleNode c, PrintWriter writer, HashMap<String, ArrayList<String>> definedVariables)
	{
		ArrayList<String> variablesUsed = new ArrayList<String>();
		String whileCondition = null;
		SimpleNode cc = (SimpleNode)c.children[0];
		if(cc.name.equals("\"BinaryOperator\""))	//While DO TIPO while(a operador b)
		{
			String binaryOperator = (String) cc.content;
			String compare1 = null;
			String compare2 = null;
			SimpleNode ccc = (SimpleNode)cc.children[1];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare1 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare1 = (String) ccc.content;
			}
			ccc = (SimpleNode)cc.children[2];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare2 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare2 = (String) ccc.content;
			}
			whileCondition = "\"While(" + removeQuotationMarks(compare1) + removeQuotationMarks(binaryOperator) + removeQuotationMarks(compare2) + ")\"";	
			writer.println(" -> " + whileCondition);
		}
		//CONDICIONADOS
		String lastLine = "";			//*ultima linha para ser adicionado ao arraylist*
		cc = (SimpleNode)c.children[1];			//CONDICIONADO TRUE
		writer.print(whileCondition);
		if(cc.name.equals("\"Block\"")){	//caso seja um block
			for(int l = 0; l < cc.children.length; l++)		
			{
				SimpleNode ccc = (SimpleNode)cc.children[l];

				lastLine = analyzeLine(ccc, definedVariables);


				if(l == (cc.children.length - 1)){
					//lastConditioned.add(lastLine);
					writer.println(" -> " + lastLine);
					writer.println(lastLine + " -> " + whileCondition);
				}
				else
					writer.print(" -> " + lastLine);
			}
		}
	}

	// While COM codigo a frente
	private void analyzeWhile(SimpleNode c, PrintWriter writer, SimpleNode after, HashMap<String, ArrayList<String>> definedVariables) {
		ArrayList<String> variablesUsed = new ArrayList<String>();
		String whileCondition = null;
		SimpleNode cc = (SimpleNode)c.children[0];
		if(cc.name.equals("\"BinaryOperator\""))	//While DO TIPO while(a operador b)
		{
			String binaryOperator = (String) cc.content;
			String compare1 = null;
			String compare2 = null;
			SimpleNode ccc = (SimpleNode)cc.children[1];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare1 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare1 = (String) ccc.content;
			}
			ccc = (SimpleNode)cc.children[2];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare2 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare2 = (String) ccc.content;
			}
			whileCondition = "\"While(" + removeQuotationMarks(compare1) + removeQuotationMarks(binaryOperator) + removeQuotationMarks(compare2) + ")\"";	
			writer.println(" -> " + whileCondition);
		}
		//CONDICIONADOS
		String lastLine = "";			//*ultima linha para ser adicionado ao arraylist*
		cc = (SimpleNode)c.children[1];			//CONDICIONADO TRUE
		writer.print(whileCondition);
		if(cc.name.equals("\"Block\"")){	//caso seja um block
			for(int l = 0; l < cc.children.length; l++)		
			{
				SimpleNode ccc = (SimpleNode)cc.children[l];

				lastLine = analyzeLine(ccc, definedVariables);


				if(l == (cc.children.length - 1)){
					//lastConditioned.add(lastLine);
					writer.println(" -> " + lastLine + " -> " + analyzeLine(after, definedVariables));
					writer.println(lastLine + " -> " + whileCondition);
				}
				else
					writer.print(" -> " + lastLine);
			}
		}
	}

	// If COM codigo a frente
	private void analyzeIf(SimpleNode c, PrintWriter writer, SimpleNode after, HashMap<String, ArrayList<String>> definedVariables) { 
		ArrayList<String> variablesUsed = new ArrayList<String>();
		String lastConditioned = null;
		//CONDICAO
		String ifCondition = null;
		SimpleNode cc = (SimpleNode)c.children[0];
		if(cc.name.equals("\"BinaryOperator\""))	//IF DO TIPO if(a operador b)
		{
			String binaryOperator = (String) cc.content;
			String compare1 = null;
			String compare2 = null;
			SimpleNode ccc = (SimpleNode)cc.children[1];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare1 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare1 = (String) ccc.content;
			}
			ccc = (SimpleNode)cc.children[2];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare2 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare2 = (String) ccc.content;
			}
			ifCondition = "\"If(" + removeQuotationMarks(compare1) + removeQuotationMarks(binaryOperator) + removeQuotationMarks(compare2) + ")\"";	
			writer.println(" -> " + ifCondition);
		}
		//CONDICIONADOS
		String lastLine = "";			//*ultima linha para ser adicionado ao arraylist*
		cc = (SimpleNode)c.children[1];			//CONDICIONADO TRUE
		writer.print(ifCondition);
		if(cc.name.equals("\"Block\"")){	//caso seja um block
			for(int l = 0; l < cc.children.length; l++)		
			{
				SimpleNode ccc = (SimpleNode)cc.children[l];

				lastLine = analyzeLine(ccc, definedVariables);

				if(l == 0){
					if(l == (cc.children.length - 1))
					{
						writer.print(" -> " + lastLine);
						writer.println("[label=\"true\"]");
						writer.println(lastLine + " -> " + analyzeLine(after,definedVariables));
					}
					else
					{
						writer.print(" -> " + lastLine);
						writer.println("[label=\"true\"]");
						lastConditioned = lastLine;
					}
				}
				else if(l == 1)
				{
					if(l == (cc.children.length - 1))
					{
						writer.println(lastConditioned + " -> " + lastLine + "->" + analyzeLine(after, definedVariables));
					}
					else
					{
						writer.print(lastConditioned + " -> " + lastLine);
						lastConditioned = null;
					}					
				}
				else if(l == (cc.children.length - 1)){
					writer.println("->" + lastLine + " -> " + analyzeLine(after, definedVariables));
				}
				else
					writer.print(" -> " + lastLine);
			}
		}
		else{								//caso seja uma linha so -> ligar codigo dentro do if ao restante codigo do block
			lastLine = analyzeLine(cc, definedVariables);
			writer.print(" -> " + lastLine);
			//lastConditioned.add(lastLine);
			writer.println("[label=\"true\"]");
			writer.println(lastLine + " -> " + analyzeLine(after, definedVariables));
		}

		if(c.children.length > 2)
		{
			cc = (SimpleNode)c.children[2];			//CONDICIONADO FALSE(caso exista)
			writer.print(ifCondition);
			if(cc.name.equals("\"Block\"")){	//caso seja um bloco
				for(int l = 0; l < cc.children.length; l++)	
				{
					SimpleNode ccc = (SimpleNode)cc.children[l];

					lastLine = analyzeLine(ccc, definedVariables);

					if(l == 0){
						if(l == (cc.children.length - 1))
						{
							writer.print(" -> " + lastLine);
							writer.println("[label=\"false\"]");
							writer.println(lastLine + " -> " + analyzeLine(after, definedVariables));
						}
						else
						{
							writer.print(" -> " + lastLine);
							writer.println("[label=\"false\"]");
							lastConditioned = lastLine;
						}
					}
					else if(l == 1)
					{
						if(l == (cc.children.length - 1))
						{
							writer.println(lastConditioned + " -> " + lastLine + "->" + analyzeLine(after, definedVariables));
						}
						else
						{
							writer.print(lastConditioned + " -> " + lastLine);
							lastConditioned = null;
						}					
					}
					else if(l == (cc.children.length - 1)){
						writer.println("->" + lastLine + " -> " + analyzeLine(after, definedVariables));
					}
					else
						writer.print(" -> " + lastLine);
				}
			}
			else{								//caso seja uma linha so
				lastLine = analyzeLine(cc, definedVariables);
				writer.print(" -> " + lastLine);
				//lastConditioned.add(lastLine);
				writer.print("[label=\"false\"]");
				writer.println(lastLine + " -> " + analyzeLine(after, definedVariables));
			}
		}
		else ////////////////////////////////// Quando nao ha false, pode-se saltar o if
		{
			writer.println(ifCondition + " -> " + analyzeLine(after, definedVariables) + "[label=\"false\"]");
		}

	}

	// If SEM codigo a frente
	private void analyzeIf(SimpleNode c, PrintWriter writer, HashMap<String, ArrayList<String>> definedVariables) {
		ArrayList<String> variablesUsed = new ArrayList<String>();
		ArrayList<String> lastConditioned = new ArrayList<String>();
		//CONDICAO
		String ifCondition = null;
		SimpleNode cc = (SimpleNode)c.children[0];
		if(cc.name.equals("\"BinaryOperator\""))	//IF DO TIPO if(a operador b)
		{
			String binaryOperator = (String) cc.content;
			String compare1 = null;
			String compare2 = null;
			SimpleNode ccc = (SimpleNode)cc.children[1];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare1 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare1 = (String) ccc.content;
			}
			ccc = (SimpleNode)cc.children[2];
			if(ccc.name.equals("\"VariableRead\""))
			{
				SimpleNode cccc = (SimpleNode)ccc.children[1];
				compare2 = (String) cccc.content;
				if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
					variablesUsed.add(removeQuotationMarks(cccc.content));
			}	
			else if(ccc.name.equals("\"Literal\""))
			{
				compare2 = (String) ccc.content;
			}
			ifCondition = "\"If(" + removeQuotationMarks(compare1) + removeQuotationMarks(binaryOperator) + removeQuotationMarks(compare2) + ")\"";	
			writer.println(" -> " + ifCondition);
		}
		//CONDICIONADOS
		String lastLine = "";			
		cc = (SimpleNode)c.children[1];			//CONDICIONADO TRUE
		writer.print(ifCondition);
		if(cc.name.equals("\"Block\"")){	//caso seja um block
			for(int l = 0; l < cc.children.length; l++)		
			{
				lastLine = " -> " + analyzeLine(cc, definedVariables);
				writer.print(lastLine);

				if(l == (cc.children.length - 1)){
					lastConditioned.add(lastLine);
					writer.println("[label=\"true\"]");
				}
			}
		}
		else{								//caso seja uma linha so
			lastLine = analyzeLine(cc, definedVariables);
			writer.print(" -> " + lastLine);
			lastConditioned.add(lastLine);
			writer.println("[label=\"true\"]");
		}

		if(c.children.length > 2)
		{
			cc = (SimpleNode)c.children[2];			//CONDICIONADO FALSE(caso exista)
			writer.print(ifCondition);
			if(cc.name.equals("\"Block\"")){	//caso seja um bloco
				for(int l = 0; l < cc.children.length; l++)	
				{
					lastLine = " -> " + analyzeLine(cc, definedVariables);
					writer.print(lastLine);

					if(l == (cc.children.length - 1)){
						lastConditioned.add(lastLine);
						writer.println("[label=\"false\"]");
					}
				}
			}
			else{								//caso seja uma linha so
				lastLine = analyzeLine(cc,definedVariables);
				writer.print(" -> " + lastLine);
				lastConditioned.add(lastLine);
				writer.println("[label=\"false\"]");
			}
		}	
	}

	
	private String analyzeLine(SimpleNode c, HashMap<String,ArrayList<String>> definedVariables){
		String str = "";
		ArrayList<String>variablesUsed = new ArrayList<String>();
		/////////////////////////////////////////////////////// int a = 1;
		if(c.name.equals("\"LocalVariable\""))
		{
			SimpleNode cc = (SimpleNode)c.children[0];
			if(cc.name.equals("\"TypeReference\""))
			{
				str += "\"" + removeQuotationMarks(cc.content) + " " + removeQuotationMarks(c.content);
				if(!variablesUsed.contains(removeQuotationMarks(c.content)))
					variablesUsed.add(removeQuotationMarks(c.content));
			}

			else if(cc.name.equals("\"ArrayTypeReference\"")){ 
				SimpleNode ccc = (SimpleNode)cc.children[0];
				str += "\"" + removeQuotationMarks(ccc.content) + "[] " + removeQuotationMarks(c.content);
				if(!variablesUsed.contains(removeQuotationMarks(c.content)))
					variablesUsed.add(removeQuotationMarks(c.content));

			}

			if(c.children.length == 1){
				String temp = ";" + "\"";	
				str = str + temp;
			}
			else{								
				cc = (SimpleNode)c.children[1];
				if(cc.name.equals("\"Literal\"")){
					String temp = " = " + removeQuotationMarks(cc.content);
					temp = temp.replaceAll("\"", "\\\\\"");
					temp += ";" + "\"";	
					str = str + temp;
				}
				else if(cc.name.equals("\"BinaryOperator\""))
				{
					String binaryOperator = (String) cc.content;
					String compare1 = null;
					String compare2 = null;
					SimpleNode ccc = (SimpleNode)cc.children[1];
					if(ccc.name.equals("\"VariableRead\""))
					{
						SimpleNode cccc = (SimpleNode)ccc.children[1];
						compare1 = (String) cccc.content;
						if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
							variablesUsed.add(removeQuotationMarks(cccc.content));
					}	
					else if(ccc.name.equals("\"Literal\""))
					{
						compare1 = (String) ccc.content;
					}
					ccc = (SimpleNode)cc.children[2];
					if(ccc.name.equals("\"VariableRead\""))
					{
						SimpleNode cccc = (SimpleNode)ccc.children[1];
						compare2 = (String) cccc.content;
						if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
							variablesUsed.add(removeQuotationMarks(cccc.content));
					}	
					else if(ccc.name.equals("\"Literal\""))
					{
						compare2 = (String) ccc.content;
					}

					str += " = " + removeQuotationMarks(compare1) + removeQuotationMarks(binaryOperator) + removeQuotationMarks(compare2) + "\"";

				}

				else if(cc.name.equals("\"NewArray\"")){
					String temp2 = "";
					temp2 += "= {";
					for(int i2 = 1;i2< cc.children.length;i2++){
						if(i2==1){
							SimpleNode arraycontent = (SimpleNode) cc.children[i2];
							temp2 += removeQuotationMarks(arraycontent.content);


						}
						else{
							SimpleNode arraycontent = (SimpleNode) cc.children[i2];

							temp2 += ","+ removeQuotationMarks(arraycontent.content);

						}

					}
					temp2 = temp2.replaceAll("\"", "\\\\\"");
					temp2 += "};" + "\"";
					str = str + temp2;
				}
			}
			for(int j = 0; j < variablesUsed.size(); j++)
			{
				addToVariables(variablesUsed.get(j), str, definedVariables);
			}
			return str;
		}

		else if(c.name.equals("\"Invocation\""))
		{
			SimpleNode cc = (SimpleNode)c.children[2]; /////////////////////////////////////////////////////// System.out.println()
			if(cc.name.equals("\"ExecutableReference\""))
			{
				if(cc.content.equals("\"println\"")) 
				{
					cc = (SimpleNode)c.children[3];
					if(cc.name.equals("\"VariableRead\"")) //Print pode ser de uma variavel ou texto(else)
					{
						SimpleNode ccc = (SimpleNode)cc.children[1];
						str = "\"System.out.println(" + removeQuotationMarks(ccc.content) + ");" + "\"";
						if(!variablesUsed.contains(removeQuotationMarks(ccc.content)))
							variablesUsed.add(removeQuotationMarks(ccc.content));
					}
					else
					{
						for(int j = 0; j < variablesUsed.size(); j++)
						{
							addToVariables(variablesUsed.get(j), str, definedVariables);
						}
						return str = "\"System.out.println(" + "\\" + "\"" + removeQuotationMarks(removeQuotationMarks(cc.content))+ "\\" + "\"" + ");" + "\"";
					}
				}
			}
			cc = (SimpleNode)c.children[1]; /////////////////////////////FUNCAO
			if(cc.name.equals("\"ExecutableReference\""))
			{
				String function = removeQuotationMarks(cc.content);
				str += "\"" + function + "(";
				for (int j = 2; j < c.children.length; j++)
				{
					cc = (SimpleNode)c.children[j];
					if(cc.name.equals("\"VariableRead\""))
					{
						SimpleNode ccc = (SimpleNode)cc.children[1];
						if(ccc.name.equals("\"LocalVariableReference\""))
						{
							if(j == 2)
								str += removeQuotationMarks(ccc.content);
							else
								str += ", " + removeQuotationMarks(ccc.content);

							if(!variablesUsed.contains(removeQuotationMarks(ccc.content)))
								variablesUsed.add(removeQuotationMarks(ccc.content));
						}
					}
				}
				for(int j = 0; j < variablesUsed.size(); j++)
				{
					addToVariables(variablesUsed.get(j), str, definedVariables);
				}
				return str += ") \"";
			}
		}
		//////////////////////////////////////////// mudar valor vari�vel
		else if(c.name.equals("\"Assignment\""))
		{
			str += "\"";
			SimpleNode cc = (SimpleNode)c.children[1];

			if(cc.name.equals("\"VariableWrite\""))
			{
				SimpleNode ccc = (SimpleNode)cc.children[1];
				if(ccc.name.equals("\"LocalVariableReference\""))
				{
					if(!variablesUsed.contains(removeQuotationMarks(ccc.content)))
						variablesUsed.add(removeQuotationMarks(ccc.content));
					str += removeQuotationMarks(ccc.content) + " = ";
				}
			}
			if(cc.name.equals("\"ArrayWrite\"")){

				SimpleNode ccc = (SimpleNode)cc.children[1];
				SimpleNode ttt = (SimpleNode)cc.children[2];
				if(ccc.name.equals("\"VariableRead\""))
				{
					SimpleNode cccc = (SimpleNode)ccc.children[1];
					if(cccc.name.equals("\"LocalVariableReference\""))
					{
						if(!variablesUsed.contains(removeQuotationMarks(cccc.content)))
							variablesUsed.add(removeQuotationMarks(cccc.content));
						str+=removeQuotationMarks(cccc.content) + "[";

					}
				}
				if(ttt.name.equals("\"Literal\""))
				{
					str+=removeQuotationMarks(ttt.content) + "]=";
				}

			}
			cc = (SimpleNode)c.children[2];
			if(cc.name.equals("\"Literal\""))
			{
				String temperino="";
				temperino = removeQuotationMarks(cc.content) + ";";
				temperino = temperino.replaceAll("\"", "\\\\\"");
				str = str + temperino;
			}
			str += "\"";

			for(int j = 0; j < variablesUsed.size(); j++)
			{
				addToVariables(variablesUsed.get(j), str, definedVariables);
			}
			return str;
		}

		////////////////////////////////////////// operador do tipo _++
		else if(c.name.equals("\"UnaryOperator\""))
		{
			String operator = (String) c.content;
			SimpleNode cc = (SimpleNode) c.children[1];
			if(cc.name.equals("\"VariableRead\""))
			{
				SimpleNode ccc = (SimpleNode) cc.children[1];
				if(ccc.name.equals("\"LocalVariableReference\""))
				{
					if(!variablesUsed.contains(removeQuotationMarks(ccc.content)))
						variablesUsed.add(removeQuotationMarks(ccc.content));
					str += "\"" + removeQuotationMarks(ccc.content) + (removeQuotationMarks(operator)).substring(1, removeQuotationMarks(operator).length()) + "\"";
				}
			}
		}
		for(int j = 0; j < variablesUsed.size(); j++)
		{
			addToVariables(variablesUsed.get(j), str, definedVariables);
		}
		return str;
	}

	public void showGraph()
	{
		GraphViz gv = new GraphViz();
		String input = "dotfile.dot";
		gv.readSource(input);

		String type = "png";

		File out = new File("output." + type); 

		gv.writeGraphToFile( gv.getGraph(gv.getDotSource(), type), out );

		Desktop dt = Desktop.getDesktop();
		try {
			dt.open(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/* JavaCC - OriginalChecksum=dc9708b1c46ec8e86a717c9e8819eba5 (do not edit this line) */