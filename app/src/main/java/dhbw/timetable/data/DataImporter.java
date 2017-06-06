package dhbw.timetable.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class DataImporter {

    private final static String baseURL = "https://rapla.dhbw-stuttgart.de/rapla";
    private String pageContent, key;
    private Map<TimelessDate, ArrayList<Appointment>> globals;

    DataImporter(String key, boolean global) {
        this.key = key;
        globals = global ? TimetableManager.getInstance().getGlobals() : TimetableManager.getInstance().getLocals();
    }

    /**
     * Downloads a html site and imports the content to Appointment DataStructure
     * @throws Exception
     */
    void importAll(GregorianCalendar startDate, GregorianCalendar endDate) throws Exception {
        GregorianCalendar tempDate = (GregorianCalendar) startDate.clone();
        do {
            URLConnection webConnection = new URL(baseURL
                    + "?key=" + key
                    + "&day=" + tempDate.get(Calendar.DAY_OF_MONTH)
                    + "&month=" + (tempDate.get(Calendar.MONTH) + 1)
                    + "&year=" + tempDate.get(Calendar.YEAR)
            ).openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(webConnection
                    .getInputStream(), StandardCharsets.UTF_8));
            pageContent = "";

            // read the whole page
            String originLine;
            while ((originLine = br.readLine()) != null) pageContent += originLine + "\n";
            br.close();

            evaluateTableBody(tempDate);

            DateHelper.NextWeek(tempDate);
        } while (!DateHelper.IsDateOver(tempDate, endDate));
    }

    private void evaluateTableBody(GregorianCalendar currDate) throws SAXException, IOException, ParserConfigurationException {
        // trim and filter to correct tbody inner HTML
        pageContent = ("<?xml version=\"1.0\"?>\n" + pageContent.substring(pageContent.indexOf("<tbody>"), pageContent.lastIndexOf("</tbody>") + 8)).replaceAll("&nbsp;", "&#160;").replaceAll("<br>", "<br/>");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(pageContent.getBytes("utf-8"))));
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getDocumentElement().getChildNodes();
        Node tableRow;
        for (int temp = 0; temp < nList.getLength(); temp++) {
            tableRow = nList.item(temp);
            if (tableRow.getNodeType() == Node.ELEMENT_NODE) evaluateTableRow(tableRow, (GregorianCalendar) currDate.clone());
        }
    }

    private void evaluateTableRow(Node tableRow, GregorianCalendar currDate) {
        NodeList cells = tableRow.getChildNodes();
        for(int i = 0; i < cells.getLength(); i++) {
            Node cell = cells.item(i);
            // filter th and other crap
            if (cell.getNodeType() == Node.ELEMENT_NODE && cell.getNodeName().equals("td")) {
                Element element = (Element) cell;
                String type = element.getAttribute("class");
                if (type.startsWith("week_block")) {
                    importWeekBlock(cell, currDate);
                } else if (type.startsWith("week_separatorcell")) {
                    DateHelper.AddDays(currDate, 1);
                }
            }
        }
    }

    private void importWeekBlock(Node block, GregorianCalendar date) {
        Node aNode = block.getFirstChild();
        NodeList aChildren = aNode.getChildNodes();

        // Filter &#160;
        String timeData = ((CharacterData) aChildren.item(0)).getData();
        String time = timeData.substring(0, 5).concat(timeData.substring(6));
        String course = "";
        String info = "";
        if (aChildren.item(2).getNodeType() == Node.ELEMENT_NODE) {
            course = "No course specified";
            info = importInfoFromSpan(aChildren.item(2).getChildNodes().item(4).getChildNodes());
        } else {
            course = ((CharacterData) aChildren.item(2)).getData();
            info = importInfoFromSpan(aChildren.item(3).getChildNodes().item(4).getChildNodes());
        }

        Appointment a = new Appointment(time, date, course, info);

        TimetableManager.getInstance().insertAppointment(globals, date, a);
    }

    private String importInfoFromSpan(NodeList spanTableRows) {
        String tutor = "";
        String resource = "";
        for(int i = 0; i < spanTableRows.getLength(); i++) {
            Node row = spanTableRows.item(i);
            if (row.getNodeType() == Node.ELEMENT_NODE) {
                NodeList cells = row.getChildNodes();
                for(int x = 0; x < cells.getLength(); x++) {
                    Node cell =  cells.item(x);
                    if(cell.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) cell;
                        String type = element.getAttribute("class");
                        if (type.contains("label")) {
                            if (element.getTextContent().equalsIgnoreCase("Ressourcen:")) {
                                resource = "Ressourcen: " + cell.getNextSibling().getNextSibling().getTextContent().trim().split(" ")[0];
                            } else if (element.getTextContent().equalsIgnoreCase("Personen:")) {
                                tutor = "Personen: " + cell.getNextSibling().getNextSibling().getTextContent();
                            }
                            // Ignore Bemerkung, zuletzt geändert, Veranstaltungsname
                        } else if (type.contains("value")) {
                            // ignore
                        } else {
                            // TODO Remove warnings after intense testing
                            System.out.println("[WARN] Unidentified classname of row found in span table: " + type);
                            System.out.println("row nodeName " + row.getNodeName());
                            System.out.println("cell nodeName " + cell.getNodeName());
                            System.out.println("element nodeName " + element.getNodeName());
                        }
                    }
                }
            }
        }
        return (resource + " " + tutor).trim();
    }

}
