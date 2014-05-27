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
public class UnitsUnmarshaller implements Unmarshaller<Units, StaxUnmarshallerContext> {
	@Override
	public Units unmarshall(StaxUnmarshallerContext context) throws Exception {
		Units oResult = new Units();
		while (true) {
			XMLEvent xmlEvent = context.nextEvent();
			if (xmlEvent.isEndDocument()) return oResult;

			if (xmlEvent.isAttribute() || xmlEvent.isStartElement()) {
				if (context.testExpression("temperature")) {
					oResult.setTemperature( StringStaxUnmarshaller.getInstance().unmarshall(context) );
					continue;
				} else if (context.testExpression("distance")) {
					oResult.setDistance( StringStaxUnmarshaller.getInstance().unmarshall(context) );
					continue;
				} else if (context.testExpression("pressure")) {
					oResult.setPressure( StringStaxUnmarshaller.getInstance().unmarshall(context) );
					continue;
				} else if (context.testExpression("speed")) {
					oResult.setSpeed( StringStaxUnmarshaller.getInstance().unmarshall(context) );
					continue;
				}
			} else if (xmlEvent.isEndElement()) {
				return oResult;
			}
		}
	}

	private static UnitsUnmarshaller	instance;

	public static UnitsUnmarshaller getInstance() {
		if (instance == null) instance = new UnitsUnmarshaller();
		return instance;
	}
}
