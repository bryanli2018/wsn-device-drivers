package de.uniluebeck.itm.metadatenservice;

import de.uniluebeck.itm.metadaten.metadatenservice.entity.Node;
import de.uniluebeck.itm.metadaten.metadatenservice.metadatacollector.IMetaDataCollector;

public interface iMetaDatenService {
	//TODO i gro� 
	
	/**
	 * F�gt dem MetaDatenService einen MetaDatenCollector hinzu, der dann seinerseits
	 * die Daten des Knoten regelm��ig aktualisiert.
	 * @param mdcollector
	 */
	public void addMetaDataCollector(IMetaDataCollector mdcollector);
	
	/**
	 * Entfernt  einen MetaDatenCollector aus  dem MetaDatenService.
	 * 
	 * @param mdcollector
	 */
	public void removeMetaDataCollector(IMetaDataCollector mdcollector);
	
	/**
	 * F�gt den Knoten dem Verzeichnis hinzu
	 * @param node
	 * @param callback
	 */
	public void addNode (Node node, final AsyncCallback<String> callback );
	
	/**
	 * Entfernt den Sensorknoten aus dem Verzeichnis
	 * @param node
	 * @param callback
	 */
	public void removeNode (Node node, final AsyncCallback<String> callback );
	
	/**
	 * Aktualisiert den TimeStamp des Sensorknotens im Verezeichnis
	 * @param node
	 * @param callback
	 */
	public void refreshNode (Node node, final AsyncCallback<String> callback );
	
	
	

}
