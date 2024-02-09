package app.utils;

import java.util.ArrayList;
import java.util.List;

public class DataFlowGraphGenerator {
  private List<String> methods = new ArrayList<>();
  private List<String> variables = new ArrayList<>();
  private List<String> edges = new ArrayList<>();
  private String params =
      "size =\"12, 9\";\n"
          + "\tdpi = 300;\n"
          + "\trankdir=LR;\n"
          + "\toverlap=false;\n"
          + "\tsplines=true;\n";

  private String methodNodeStyle = "node [shape=circle, style=filled, color=gainsboro]\n";
  private String variableNodeStyle = "node [shape=box, style=rounded, color=lightgrey]\n";
  private String edgeStyle = "edge [color=black, fontname=\"Helvetica\", fontsize=10]\n";

  public void addMethod(String method, String label) {
    methods.add(method + (label.isEmpty() ? "" : " [label=\"" + label + "\"]"));
  }

  public List<String> getMethods() {
    return methods;
  }

  public void addVariable(String variable, String label) {
    variables.add(variable + (label.isEmpty() ? "" : " [label=\"" + label + "\"]"));
  }

  public List<String> getVariables() {
    return variables;
  }

  public void addEdge(String from, String to, String label) {
    edges.add(from + " -> " + to + (label.isEmpty() ? "" : " [label=\"" + label + "\"]"));
  }

  public List<String> getEdges() {
    return edges;
  }

  public String generateDotGraph() {
    StringBuilder dotGraph = new StringBuilder("digraph DataFlow {\n");
    dotGraph.append("\t" + params);
    dotGraph.append("\t" + methodNodeStyle);
    for (String method : methods) {
      dotGraph.append("\t" + method + "\n");
    }
    dotGraph.append("\t" + variableNodeStyle);
    for (String variable : variables) {
      dotGraph.append("\t" + variable + "\n");
    }
    dotGraph.append("\t" + edgeStyle);
    for (String edge : edges) {
      dotGraph.append("\t" + edge + "\n");
    }
    dotGraph.append("}");
    return dotGraph.toString();
  }
}
