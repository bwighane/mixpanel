package io.flutuate.plugin.mixpanel;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class MixpanelPlugin
        implements MethodCallHandler
{
    private static final String name = "flutuate.io/plugins/mixpanel";
    private static final Map<String, Object> EMPTY_HASHMAP = new HashMap<>();

    private final PluginRegistry.Registrar registrar;
    private MixpanelAPI mixpanel;

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), MixpanelPlugin.name);
        channel.setMethodCallHandler(new MixpanelPlugin(registrar));
    }

    private MixpanelPlugin(PluginRegistry.Registrar registrar) {
        this.registrar = registrar;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "getInstance":
                getInstance(call, result);
                break;
            case "flush":
                flush(result);
                break;
            case "track":
                track(call, result);
                break;
            case "trackMap":
                trackMap(call, result);
                break;
            case "getDeviceInfo":
                getDeviceInfo(result);
                break;
            case "getDistinctId":
                getDistinctId(result);
                break;
            case "optInTracking":
                optInTracking(result);
                break;
            case "optOutTracking":
                optOutTracking(result);
                break;
            case "reset":
                reset(result);
                break;
            case "identify":
                identify(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void getInstance(MethodCall call, Result result) {
        String token = call.argument("token");
        if (call.hasArgument("optOutTrackingDefault")) {
            Boolean optOutTrackingDefault = call.<Boolean>argument("optOutTrackingDefault");
            mixpanel = MixpanelAPI.getInstance(registrar.context(), token, optOutTrackingDefault == null ? false : optOutTrackingDefault);
        } else
            mixpanel = MixpanelAPI.getInstance(registrar.context(), token);
        result.success(mixpanel.hashCode());
    }

    private void flush(Result result) {
        mixpanel.flush();
        result.success(null);
    }

    private void track(MethodCall call, Result result) {
        String eventName = call.argument("eventName");
        Map<String, Object> mapProperties = call.<HashMap<String, Object>>argument("properties");
        JSONObject properties;
        try {
            properties = extractJSONObject(mapProperties == null ? EMPTY_HASHMAP : mapProperties);
        } catch (JSONException e) {
            result.error(e.getClass().getName(), e.toString(), "");
            return;
        }

        mixpanel.track(eventName, properties);

        result.success(null);
    }

    private void trackMap(MethodCall call, Result result) {
        String eventName = call.argument("eventName");
        Map<String, Object> properties = call.<HashMap<String, Object>>argument("properties");
        mixpanel.trackMap(eventName, properties);
        result.success(null);
    }

    @SuppressWarnings("unchecked")
    private JSONObject extractJSONObject(Map<String, Object> properties) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (properties != null) {
            for (String key : properties.keySet()) {
                Object value = properties.get(key);
                if (value instanceof Map<?, ?>) {
                    value = extractJSONObject((Map<String, Object>) value);
                }
                jsonObject.put(key, value);
            }
        }
        return jsonObject;
    }

    private void getDeviceInfo(Result result) {
        Map<String, String> map = mixpanel.getDeviceInfo();
        result.success(map);
    }

    private void getDistinctId(Result result) {
        result.success(mixpanel.getDistinctId());
    }

    private void optInTracking(Result result) {
        mixpanel.optInTracking();
        result.success(null);
    }

    private void optOutTracking(Result result) {
        mixpanel.optOutTracking();
        result.success(null);
    }

    private void reset(Result result) {
        mixpanel.reset();
        result.success(null);
    }

    private void identify(MethodCall call, Result result) {
        String distinctId = call.argument("distinctId");
        mixpanel.identify(distinctId);
        result.success(null);
    }
}
