package org.apache.camel.language.datasonnet;

import java.util.*;

import com.datasonnet.document.DefaultDocument;
import com.datasonnet.document.Document;
import com.datasonnet.document.MediaTypes;
import com.datasonnet.header.Header;
import com.datasonnet.spi.DataFormatService;
import com.datasonnet.spi.Library;
import com.datasonnet.spi.PluginException;
import org.apache.camel.Exchange;
import sjsonnet.Materializer;
import sjsonnet.Val;

public class CML extends Library {

    public static final ThreadLocal<Exchange> exchange = new ThreadLocal<Exchange>();

    @Override
    public String namespace() {
        return "cml";
    }

    @Override
    public Set<String> libsonnets() {
        return Collections.emptySet();
    }

    @Override
    public java.util.Map<String, Val.Func> functions(DataFormatService dataFormats, Header header) {
        return Collections.unmodifiableMap(new HashMap<String, Val.Func>() {
            {
                //put(<function name>, makeSimpleFunc(<param list>, lambda function))
                put("properties", makeSimpleFunc(
                        Collections.singletonList("key"), //parameters list
                        params -> properties(params.get(0))));
                put("header", makeSimpleFunc(
                        Collections.singletonList("key"), //parameters list
                        params -> header(params.get(0), dataFormats)));
                put("exchangeProperty", makeSimpleFunc(
                        Collections.singletonList("key"), //parameters list
                        params -> exchangeProperty(params.get(0), dataFormats)));
            }
        });

    }

    public Map<String, Val.Obj> modules(DataFormatService dataFormats, Header header) {
        return Collections.emptyMap();
    }

    private Val properties(Val key) {
        if (key instanceof Val.Str) {
            return new Val.Str(exchange.get().getContext().resolvePropertyPlaceholders("{{" + key + "}}"));
        }
        throw new IllegalArgumentException("Expected String got: " + key.prettyName());
    }

    private Val header(Val key, DataFormatService dataformats) {
        if (key instanceof Val.Str) {
            return valFrom(exchange.get().getMessage().getHeader(((Val.Str) key).value()), dataformats);
        }
        throw new IllegalArgumentException("Expected String got: " + key.prettyName());
    }

    private Val exchangeProperty(Val key, DataFormatService dataformats) {
        if (key instanceof Val.Str) {
            return valFrom(exchange.get().getProperty(((Val.Str) key).value()), dataformats);
        }
        throw new IllegalArgumentException("Expected String got: " + key.prettyName());
    }

    // TODO: write to map null objs to Val.Null instead NPE
    private Val valFrom(Object obj, DataFormatService dataformats) {
        Document doc;
        if (obj instanceof Document) {
            doc = (Document) obj;
        } else {
            doc = new DefaultDocument(obj, MediaTypes.APPLICATION_JAVA);
        }

        try {
            return Materializer
                    .reverse(dataformats.thatAccepts(doc).orElseThrow(() -> new IllegalArgumentException("todo")).read(doc));
        } catch (PluginException e) {
            throw new IllegalStateException(e);
        }
    }
}
