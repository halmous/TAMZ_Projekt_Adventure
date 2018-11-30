package com.example.jhalm.tamz_projekt_adventure;

import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XMLNode {

    private String tagName;
    private String tagText;
    private List<XMLNode> childs;
    private Map<String, String> attributes;

    public XMLNode(String tagName)
    {
        this.tagName = tagName;
        this.tagText = "";
        this.childs = new ArrayList<XMLNode>();
        this.attributes = new ArrayMap<String, String>();
    }

    public void AddChild(XMLNode child)
    {
        this.childs.add(child);
    }

    public int ChildCount()
    {
        return this.childs.size();
    }

    public XMLNode GetChild(int i)
    {
        return this.childs.get(i);
    }

    public void AddAttribute(String paramName, String paramValue) { this.attributes.put(paramName, paramValue); }

    public String GetAttributeValue(String paramName) { return this.attributes.get(paramName); }

    public void SetText(String tagText){ this.tagText = tagText; }

    public String GetText() { return this.tagText; }

    public String GetName() { return this.tagName; }

}
