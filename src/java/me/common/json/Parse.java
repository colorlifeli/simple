package me.common.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.common.SimpleException;
import me.common.json.Token.TokenType;

public class Parse {

    private Tokenizer tokenizer;

    public Parse(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public static Json parseJSONObject(String jsonString) throws IOException, SimpleException {
        Tokenizer tokenizer = new Tokenizer(new BufferedReader(new StringReader(jsonString)));
        tokenizer.tokenizer();
        Parse parser = new Parse(tokenizer);
        return parser.object();
    }

    private Json object() throws SimpleException {
        Map<String,Value> map = new HashMap<String,Value>();
    	//有些json直接一开始就是数组，没有key
    	if(isToken(TokenType.START_ARRAY)){
    		return array();
    	}
    	
        tokenizer.next();
        if(isToken(TokenType.END_OBJ)){
            tokenizer.next();
            return new JObject(map);
        }else if(isToken(TokenType.STRING)){
            map = key(map);
        }
        return new JObject(map);
    }

    private Map<String, Value> key(Map<String, Value> map) throws SimpleException {
        String key = tokenizer.next().getValue();
        if(!isToken(TokenType.COLON)){
            throw new SimpleException("Invalid JSON object");
        }else {
            tokenizer.next();
            if(isPrimary()){
                //Value primary = new Primary(tokenizer.peek(0).getValue()); 
            	//20200207 resolve bug
            	Value primary = new Primary(tokenizer.next().getValue());
                map.put(key,primary);
            }else if(isToken(TokenType.START_ARRAY)){
                Value array = array();
                map.put(key,array);
            }
            if(isToken(TokenType.COMMA)){
                tokenizer.next(); //consume ','
                if(isToken(TokenType.STRING))
                    map = key(map);
            }else if(isToken(TokenType.END_OBJ)){
                tokenizer.next();
                return map;
            }

        }
        return map;
    }

    private JArray array() throws SimpleException {
        tokenizer.next(); //consume '['
        List<Value> list = new ArrayList<>();
        JArray array = null;
        if (isToken(TokenType.START_ARRAY)) {
            array = array();
            list.add(array);
            if (isToken(TokenType.COMMA)) {
                tokenizer.next(); //consume ','
                list = element(list);
            }
        } else if (isPrimary()) {
            list = element(list);
        } else if (isToken(TokenType.START_OBJ)) {
            list.add( object());
            while (isToken(TokenType.COMMA)) {
                tokenizer.next(); //consume ','
                list.add( object());
            }
        } else if (isToken(TokenType.END_ARRAY)) {
            tokenizer.next(); //consume ']'
            array =  new JArray(list);
            return array;
        }
        tokenizer.next(); //consume ']'
        array = new JArray(list);
        return array;
    }

    private List<Value> element(List<Value> list) throws SimpleException {
        list.add(new Primary(tokenizer.next().getValue()));
        if (isToken(TokenType.COMMA)) {
            tokenizer.next(); //consume ','
            if (isPrimary()) {
                list = element(list);
            } else if (isToken(TokenType.START_OBJ)) {
                list.add(object());
            } else if (isToken(TokenType.START_ARRAY)) {
                list.add(array());
            } else {
                throw new SimpleException("Invalid JSON input.");
            }
        } else if (isToken(TokenType.END_ARRAY)) {
            return list;
        } else {
            throw new SimpleException("Invalid JSON input.");
        }
        return list;
    }


    private boolean isPrimary() {
        TokenType type = tokenizer.peek(0).getType();
        return type == TokenType.BOOLEAN || type == TokenType.NULL  ||
                type == TokenType.NUMBER || type == TokenType.STRING;
    }

    private boolean isToken(TokenType startObj) {
        return tokenizer.peek(0).getType()==startObj;
    }
    
    public static void main(String[] args) throws IOException {
        String json = "{\"date\":\"20161024\",\"stories\":[{\"images\":[\"http:\\/\\/pic2.zhimg.com\\/121d38a2c723528c69bf325770fc04f1.jpg\"],\"type\":0,\"id\":8913228,\"ga_prefix\":\"102411\",\"title\":\"aa\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/a81dd2b0675d9800154cdb5a37f429ec.jpg\"],\"type\":0,\"id\":8913482,\"ga_prefix\":\"102410\",\"title\":\"bb\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/c69d7ee8bc8c543489571d6896869a6c.jpg\"],\"type\":0,\"id\":8913325,\"ga_prefix\":\"102409\",\"title\":\"cc\"},{\"images\":[\"http:\\/\\/pic2.zhimg.com\\/da74bb5016d13c52bd506476b2497f91.jpg\"],\"type\":0,\"id\":8913221,\"ga_prefix\":\"102408\",\"title\":\"dd\"},{\"images\":[\"http:\\/\\/pic4.zhimg.com\\/910ea288ebab555d2e1ab5712aea571f.jpg\"],\"type\":0,\"id\":8913218,\"ga_prefix\":\"102407\",\"title\":\"ee\"},{\"images\":[\"http:\\/\\/pic3.zhimg.com\\/f6745ea878d587a51c37a87e2e6e308a.jpg\"],\"type\":0,\"id\":8913426,\"ga_prefix\":\"102407\",\"title\":\"ff\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/94811b34d96e1aeb69117d7465ebc8fc.jpg\"],\"type\":0,\"id\":8913343,\"ga_prefix\":\"102407\",\"title\":\"gg\"},{\"images\":[\"http:\\/\\/pic1.zhimg.com\\/189828a22b2f0945f63532edb5d2c8f4.jpg\"],\"type\":0,\"id\":8913257,\"ga_prefix\":\"102407\",\"title\":\"hh\"},{\"images\":[\"http:\\/\\/pic2.zhimg.com\\/fb416d09f0406e2ecb2ba520fd6d68d9.jpg\"],\"type\":0,\"id\":8911164,\"ga_prefix\":\"102406\",\"title\":\"ii\"}],\"top_stories\":[{\"image\":\"http:\\/\\/pic3.zhimg.com\\/958b8c4950205c920ef673a885d1b1fa.jpg\",\"type\":0,\"id\":8913325,\"ga_prefix\":\"102409\",\"title\":\"jj\"},{\"image\":\"http:\\/\\/pic1.zhimg.com\\/0ad85b64b3cee1fd1e703bc900837180.jpg\",\"type\":0,\"id\":8913343,\"ga_prefix\":\"102407\",\"title\":\"kk\"},{\"image\":\"http:\\/\\/pic1.zhimg.com\\/7e50d96633c38aa343c7cc8d2c44b01c.jpg\",\"type\":0,\"id\":8913257,\"ga_prefix\":\"102407\",\"title\":\"ll\"},{\"image\":\"http:\\/\\/pic1.zhimg.com\\/e499296bce37b87ef8fcdee134329360.jpg\",\"type\":0,\"id\":8912393,\"ga_prefix\":\"102317\",\"title\":\"mm\"},{\"image\":\"http:\\/\\/pic1.zhimg.com\\/337e63021382842cd6c890f589a8d438.jpg\",\"type\":0,\"id\":8906679,\"ga_prefix\":\"102313\",\"title\":\"nn\"}]}";
        //json = "{\"day\":\"2020-01-16\",\"open\":\"24.360\",\"high\":\"24.430\",\"low\":\"23.850\",\"close\":\"23.880\",\"volume\":\"3953763\"}";
        // ** 属性不带双引号
        json = "[{day:\"2020-01-16\",open:\"24.360\",high:\"24.430\",low:\"23.850\",close:\"23.880\",volume:\"3953763\"},{day:\"2020-01-17\",open:\"23.800\",high:\"24.180\",low:\"23.660\",close:\"23.700\",volume:\"3756469\"},{day:\"2020-01-20\",open:\"24.300\",high:\"24.300\",low:\"23.450\",close:\"23.810\",volume:\"3847605\"},{day:\"2020-01-21\",open:\"23.730\",high:\"24.130\",low:\"23.200\",close:\"23.300\",volume:\"3891750\"},{day:\"2020-01-22\",open:\"23.370\",high:\"23.370\",low:\"22.450\",close:\"23.090\",volume:\"4891264\"},{day:\"2020-01-23\",open:\"22.880\",high:\"23.210\",low:\"22.020\",close:\"22.300\",volume:\"4372890\"},{day:\"2020-02-03\",open:\"20.070\",high:\"20.070\",low:\"20.070\",close:\"20.070\",volume:\"396100\"},{day:\"2020-02-04\",open:\"18.060\",high:\"19.400\",low:\"18.060\",close:\"19.140\",volume:\"8574190\"},{day:\"2020-02-05\",open:\"19.040\",high:\"20.440\",low:\"19.000\",close:\"19.870\",volume:\"6724233\"},{day:\"2020-02-06\",open:\"19.900\",high:\"20.620\",low:\"19.790\",close:\"20.450\",volume:\"5099583\"}]";
        //json = "{day:\"2020-01-16\",open:\"24.360\",high:\"24.430\",low:\"23.850\",close:\"23.880\",volume:\"3953763\"}";
        System.out.println(Parse.parseJSONObject(json).toString());
    }

}