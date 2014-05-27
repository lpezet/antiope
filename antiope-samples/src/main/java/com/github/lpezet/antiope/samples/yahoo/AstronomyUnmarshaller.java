/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import javax.xml.stream.events.XMLEvent;

import com.github.lpezet.antiope.transform.SimpleTypeStaxUnmarshallers.StringStaxUnmarshaller;
import com.github.lpezet.antiope.transform.StaxUnmarshallerContext;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author luc
 *
 */
public class AstronomyUnmarshaller implements Unmarshaller<Astronomy, StaxUnmarshallerContext> {
	
	@Override
	public Astronomy unmarshall(StaxUnmarshallerContext context) throws Exception {
		Astronomy oResult = new Astronomy();
		while (true) {
			XMLEvent xmlEvent = context.nextEvent();
			if (xmlEvent.isEndDocument()) return oResult;

			if (xmlEvent.isAttribute() || xmlEvent.isStartElement()) {
				if (context.testExpression("sunrise")) {
					oResult.setSunrise( StringStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				} else if (context.testExpression("sunset")) {
					oResult.setSunset( StringStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				}
			} else if (xmlEvent.isEndElement()) {
				return oResult;
			}
		}
	}

	private static AstronomyUnmarshaller	instance;

	public static AstronomyUnmarshaller getInstance() {
		if (instance == null) instance = new AstronomyUnmarshaller();
		return instance;
	}
} 
