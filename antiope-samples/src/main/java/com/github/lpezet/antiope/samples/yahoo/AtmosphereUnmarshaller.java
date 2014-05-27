/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import javax.xml.stream.events.XMLEvent;

import com.github.lpezet.antiope.transform.SimpleTypeStaxUnmarshallers.DoubleStaxUnmarshaller;
import com.github.lpezet.antiope.transform.SimpleTypeStaxUnmarshallers.IntegerStaxUnmarshaller;
import com.github.lpezet.antiope.transform.StaxUnmarshallerContext;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author luc
 *
 */
public class AtmosphereUnmarshaller implements Unmarshaller<Atmosphere, StaxUnmarshallerContext> {
	@Override
	public Atmosphere unmarshall(StaxUnmarshallerContext context) throws Exception {
		Atmosphere oResult = new Atmosphere();
		while (true) {
			XMLEvent xmlEvent = context.nextEvent();
			if (xmlEvent.isEndDocument()) return oResult;

			if (xmlEvent.isAttribute() || xmlEvent.isStartElement()) {
				if (context.testExpression("visibility")) {
					oResult.setVisibility(DoubleStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				} else if (context.testExpression("humidity")) {
					oResult.setHumidity(IntegerStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				} else if (context.testExpression("pressure")) {
					oResult.setPressure(DoubleStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				} else if (context.testExpression("rising")) {
					oResult.setRising(IntegerStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				}
			} else if (xmlEvent.isEndElement()) {
				return oResult;
			}
		}
	}

	private static AtmosphereUnmarshaller	instance;

	public static AtmosphereUnmarshaller getInstance() {
		if (instance == null) instance = new AtmosphereUnmarshaller();
		return instance;
	}
}
