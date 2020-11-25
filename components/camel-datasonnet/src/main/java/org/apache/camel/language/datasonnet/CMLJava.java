package org.apache.camel.language.datasonnet;

import com.datasonnet.document.DefaultDocument;
import com.datasonnet.document.Document;
import com.datasonnet.document.MediaTypes;
import com.datasonnet.header.Header;
import com.datasonnet.spi.DataFormatService;
import com.datasonnet.spi.Library;
import com.datasonnet.spi.PluginException;
import org.apache.camel.Exchange;
import scala.collection.JavaConverters;
import scala.collection.immutable.Map;
import scala.collection.immutable.Set;
import sjsonnet.Materializer;
import sjsonnet.Std;
import sjsonnet.Val;

public class CMLJava extends Library {

    public static final ThreadLocal<Exchange> exchange = new ThreadLocal<Exchange>();

    @Override
    public String namespace() {
        return "cml";
    }

    @Override
    public Set<String> libsonnets() {
        return scala.collection.immutable.Set$.MODULE$.empty();
    }

    @Override
    public Map<String, Val.Func> functions(DataFormatService dataFormats, Header header) {

        /*
              def builtin0[R: ReadWriter](name: String, params: String*)(eval: (Array[Val], EvalScope, FileScope) => R) = {
                val paramData = params.zipWithIndex.map{case (k, i) => (k, None, i)}.toArray
                val paramIndices = params.indices.toArray
                name -> Val.Func(
                  None,
                  Params(paramData),
                  {(scope, thisFile, ev, fs, outerOffset) =>
                    implicitly[ReadWriter[R]].write(
                      eval(paramIndices.map(i => scope.bindings(i).get.force), ev, fs)
                    )
                  }
                )
              }
             Tuple2(name, Val.Obj.Member(
                add = false,
                Visibility.None,
                Val.Func(
                    None,
                    Params,
                    evalRhs?
                )
             ))
         */
        /*Std.builtin0(
                "properties",
                JavaConverters.asScalaIteratorConverter(new ArrayList<String>() {{add("key");}}.iterator()).asScala().toSeq(),
                "",
                ""
        );*/
        return null;
    }

    public Map<String, Val.Obj> modules(DataFormatService dataFormats, Header header) {
        return scala.collection.immutable.Map$.MODULE$.empty();
    }

    private String properties(Val key) {
        if (key instanceof Val.Str) {
            return exchange.get().getContext().resolvePropertyPlaceholders("{{" + key + "}}");
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
                    .reverse(dataformats.thatCanRead(doc).orElseThrow(() -> new IllegalArgumentException("todo")).read(doc));
        } catch (PluginException e) {
            throw new IllegalStateException(e);
        }
    }
}
