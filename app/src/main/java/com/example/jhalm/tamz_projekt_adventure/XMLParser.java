package com.example.jhalm.tamz_projekt_adventure;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class XMLParser extends Thread {

    private InputStream inputStream;
    private XMLNode rootNode;
    private EndHandler endHandler;

    public XMLParser(InputStream input, EndHandler endHandler)
    {
        this.inputStream = input;
        this.endHandler = endHandler;
    }

    @Override
    public void run()
    {
        //super.run();

        XmlPullParser xmlPullParser = Xml.newPullParser();
        Deque<XMLNode> stack = new ArrayDeque<XMLNode>();

        try
        {
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(this.inputStream, null);

            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT)
            {
                Log.d("TAMZ_XML", "Name=" + xmlPullParser.getName());

                if(xmlPullParser.getEventType() == XmlPullParser.START_TAG)
                {
                    XMLNode xmlNode = new XMLNode(xmlPullParser.getName());
                    this.ReadParams(xmlNode, xmlPullParser);

                    if(stack.isEmpty())
                        this.rootNode = xmlNode;
                    else
                        stack.getFirst().AddChild(xmlNode);

                    stack.addFirst(xmlNode);
                }
                else if(xmlPullParser.getEventType() == XmlPullParser.TEXT)
                {
                    stack.getFirst().SetText(xmlPullParser.getText());
                }
                else if(xmlPullParser.getEventType() == XmlPullParser.END_TAG)
                {
                    XMLNode tmpNode = stack.removeFirst();
                }
            }
        }
        catch (Exception e)
        {
            Log.d("TAMZ_Exception", e.toString());
        }

        if(this.endHandler != null)
            this.endHandler.OnEnd();
    }

    private void ReadParams(XMLNode xmlNode, XmlPullParser xmlPullParser)
    {
        for(int i = 0; i < xmlPullParser.getAttributeCount(); i++)
        {
            xmlNode.AddAttribute(xmlPullParser.getAttributeName(i), xmlPullParser.getAttributeValue(i));
        }
    }

    public XMLNode GetResult()
    {
        return this.rootNode;
    }

}
