package org.got5.tapestry5.jquery.mixins;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.got5.tapestry5.jquery.utils.JQueryUtils;
import org.got5.tapestry5.jquery.utils.TagLocation;

/**
 * <p>
 * 		This Mixin is used to call a jquery plugin "raty" that will provide rendered stars for different uses:
 * 		<ul>
 * 			<li>In a form: You can call the mixin in a Textfield or a Select, it will replace the input with a list of stars to rate.<br/>
 * 				To use it, call the component with '&lt;input type="text" t:type="textfield/select" t:id="RatyId" t:mixins="kawwa2/raty"/&gt;'.
 * 			</li>
 * 			<li>In a division: You can call the mixin in a division to display a result.<br/>
 * 				To use it, call the component with '&lt;p t:type="any" t:mixins="kawwa2/raty"&gt;Any text you want to display&lt;/p&gt;'.
 * 			</li>
 * 		</ul>
 * </p>
 * 
 * <p>
 * 		There is optionnal parameters to call:
 * 		<ul>
 * 			<li>t:ratyOptions="&lt;&lt;!&gt;&gt;" ==> Replace &lt;&lt;!&gt;&gt; by the JSONObject containing the options you want to pass onto the JQuery plugin "raty" used for the rate. This object will overide any conflicting raty configuration generated by other tapestry parameters.</li>
 * 			<li>t:ratyRates="&lt;&lt;!&gt;&gt;" ==> Replace &lt;&lt;!&gt;&gt; by the list of rates you want to be displayed/available. The SelectModel values must start from (0 if you want the possiblity of no rate or 1) and increase its value by 1 for each rate in case of an input to synchronize both.</li>
 * 			<li>t:ratyValue="&lt;&lt;!&gt;&gt;" ==> Replace &lt;&lt;!&gt;&gt; by the value want to be setted for the raty plugin.</li>
 * 			<li>t:ratyStarOn="&lt;&lt;!&gt;&gt;" ==> Replace &lt;&lt;!&gt;&gt; by the asset containing the image you want to be displayed for an active star icon.</li>
 * 			<li>t:ratyStarOff="&lt;&lt;!&gt;&gt;" ==> Replace &lt;&lt;!&gt;&gt; by the asset containing the image you want to be displayed for an inactive star icon.</li>
 * 			<li>t:ratyCancelOn="&lt;&lt;!&gt;&gt;" ==> Replace &lt;&lt;!&gt;&gt; by the asset containing the image you want to be displayed for an active cancel icon.</li>
 * 			<li>t:ratyCancelOff="&lt;&lt;!&gt;&gt;" ==> Replace &lt;&lt;!&gt;&gt; by the asset containing the image you want to be displayed for an inactive cancel icon.</li>
 * 			<li>t:ratyLocation="&lt;&lt;!&gt;&gt;" ==> Replace &lt;&lt;!&gt;&gt; by the string containing the literal ("before" or "after") expression for the relative location of the Raty mixin refering to the input.</li>
 * 		</ul>
 * </p>
 */

@MixinAfter
public class Raty {

	@Parameter
	private JSONObject ratyOptions;
	
	@Parameter
	private SelectModel ratyRates;
	
	@Parameter
	private Float ratyValue;
	
	@Parameter(value="${assets.path}/mixins/raty/star-on.png", defaultPrefix=BindingConstants.ASSET)
	private Asset ratyStarOn;
	
	@Parameter(value="${assets.path}/mixins/raty/star-off.png", defaultPrefix=BindingConstants.ASSET)
	private Asset ratyStarOff;
	
	@Parameter(value="${assets.path}/mixins/raty/cancel-on.png", defaultPrefix=BindingConstants.ASSET)
	private Asset ratyCancelOn;
	
	@Parameter(value="${assets.path}/mixins/raty/cancel-off.png", defaultPrefix=BindingConstants.ASSET)
	private Asset ratyCancelOff;
	
	@Parameter
	private TagLocation ratyLocation;
	
	@InjectContainer
	private ClientElement container;
	
	@Inject
	private JavaScriptSupport javaScriptSupport;
	
	@AfterRender
	public void afterRender() {
		
		String target = container.getClientId();
		String id = javaScriptSupport.allocateClientId(target + "Raty");
		
	    JSONObject opt = new JSONObject();
	    opt.put("id", id);
	    opt.put("target", target);
	    opt.put("location", ratyLocation == null ? TagLocation.AFTER.getValue() : ratyLocation.getValue());
	    
		JSONObject params = new JSONObject();

		//Addition of icons
		params.put("path", "");
		
		params.put("starOn", ratyStarOn.toClientURL());
		params.put("starOff", ratyStarOff.toClientURL());
		params.put("cancelOn", ratyCancelOn.toClientURL());
		params.put("cancelOff", ratyCancelOff.toClientURL());
		
		//Synchronisation to input/select; or setting in "ReadOnly"
	    JSONObject defaultFunc = new JSONObject();
		if(TextField.class.isInstance(container) || Select.class.isInstance(container))
		{
			defaultFunc.put("target", "#" + target);
			defaultFunc.put("targetKeep", true);
			defaultFunc.put("targetType", "number");
			
			opt.put("hide", true);
		}
		else
		{
			defaultFunc.put("readOnly", true);
			
			opt.put("hide", false);
		}
		
		JQueryUtils.merge(params, defaultFunc);
		
		//Addition of the rates if setted and number of stars
		if(ratyRates != null)
		{
			int number = 0;
			boolean cancel = false;
			JSONArray hints = new JSONArray();
			
			for(OptionModel currentOption : ratyRates.getOptions())
			{
				if("0".equals(currentOption.getValue()) || "Cancel".equalsIgnoreCase(currentOption.getLabel()))
					cancel = true;
				else
				{
					hints.put(currentOption.getLabel());
					number++;
				}
			}
			
			params.put("number", number);
			params.put("hints", hints);
			params.put("cancel", cancel);
		}
		
		//Addition of the value if setted
		if(ratyValue != null)
		{
			params.put("score", ratyValue);
		}
		
		if(ratyOptions == null) ratyOptions = new JSONObject();
	    JQueryUtils.merge(params, ratyOptions);
	    
	    opt.put("params", params);
	    
		javaScriptSupport.require("tjq/raty").with(opt);

	}
}

