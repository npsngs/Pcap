package com.grumpycat.pcap.ui.detail;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.Config;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcaplib.data.ParseMeta;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.IOUtils;
import com.grumpycat.pcaplib.util.StrUtil;
import com.grumpycat.pcaplib.util.ThreadPool;


import java.io.EOFException;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.GzipSource;
import okio.Okio;
import okio.Source;

public class SessionDetailHolder extends BaseHolder<ParseMeta> {
    private static final ByteString NR = ByteString.of((byte) 0x0d, (byte) 0x0a);

    public static int responseTextColor;
    public static int requestTextColor;
    private int showProtocol;
    public SessionDetailHolder(View itemView) {
        super(itemView);
    }
    private View ll_subtitle;
    private View ll_card;
    private ImageView iv_icon;
    private TextView tv_headers;
    private TextView tv_content;
    private ImageView iv_image;

    private TextView tv_form, tv_raw, tv_hex;
    private static final int MODE_FORM =    1;
    private static final int MODE_RAW =     2;
    private static final int MODE_HEX =     3;
    private int showMode = MODE_FORM;
    private ParseMeta meta;
    private long dataOffset = 0;
    public void setShowProtocol(int showProtocol) {
        this.showProtocol = showProtocol;
    }

    @Override
    protected void initWithView(View itemView) {
        iv_icon = findViewById(R.id.iv_icon);
        ll_subtitle = findViewById(R.id.ll_subtitle);
        ll_card = itemView;
        tv_headers = findViewById(R.id.tv_headers);
        tv_content = findViewById(R.id.tv_content);
        iv_image = findViewById(R.id.iv_image);
        tv_hex = findViewById(R.id.tv_hex);
        tv_raw = findViewById(R.id.tv_raw);
        tv_form = findViewById(R.id.tv_form);
        tv_raw.setOnClickListener(v->setShowMode(MODE_RAW));
        tv_form.setOnClickListener(v->setShowMode(MODE_FORM));
        tv_hex.setOnClickListener(v->setShowMode(MODE_HEX));
        iv_image.setOnClickListener(v->{
            if(meta != null){
                iv_image.setEnabled(false);
                ThreadPool.runUIWorker(new ParseAction(getAdapterPosition(), meta));
            }
        });
        setShowMode(MODE_FORM);
    }

    private void setShowMode(int mode){
        tv_hex.setSelected(false);
        tv_raw.setSelected(false);
        tv_form.setSelected(false);
        switch (mode){
            case MODE_FORM:
                tv_form.setSelected(true);
                break;
            case MODE_RAW:
                tv_raw.setSelected(true);
                break;
            case MODE_HEX:
                tv_hex.setSelected(true);
                break;
        }
        showMode = mode;
        if(meta != null){
            dataOffset = 0;
            ThreadPool.runUIWorker(new ParseAction(getAdapterPosition(), meta));
        }
    }

    @Override
    protected void onBindData(BindDataGetter<ParseMeta> dataGetter) {
        meta = dataGetter.getItemData(getAdapterPosition());
        if(meta.isSend()){
            ll_card.setBackgroundResource(R.drawable.sp_card_bg);
            ll_subtitle.setBackgroundResource(R.drawable.sp_subtitle_bg);
            iv_icon.setImageResource(R.drawable.ic_upload);
            tv_headers.setTextColor(requestTextColor);
            tv_content.setTextColor(requestTextColor);
        }else{
            ll_card.setBackgroundResource(R.drawable.sp_card_bg_white);
            ll_subtitle.setBackgroundResource(R.drawable.sp_subtitle_bg_white);
            iv_icon.setImageResource(R.drawable.ic_download_hlll);
            tv_headers.setTextColor(responseTextColor);
            tv_content.setTextColor(responseTextColor);
        }
        bindData(meta);
    }



    protected void bindData(ParseMeta meta) {
        tv_headers.setVisibility(View.VISIBLE);
        tv_content.setVisibility(View.GONE);
        iv_image.setVisibility(View.GONE);
        ThreadPool.runUIWorker(new ParseAction(getAdapterPosition(), meta));
    }

    class ParseAction implements Runnable{
        private int pos;
        private ParseMeta meta;

        public ParseAction(int pos, ParseMeta meta) {
            this.pos = pos;
            this.meta = meta;
        }

        private void parse() throws  Exception{
            if(showMode == MODE_FORM){
                switch (showProtocol){
                    case Const.HTTP:
                    case Const.HTTPS:
                        try {
                            parseAsHttp();
                        }catch (EOFException e){
                            e.printStackTrace();
                            parseAsRaw();
                        }
                        break;
                    default:
                        parseAsRaw();
                        break;
                }
            }else{
                parseAsRaw();
            }
        }
        private void parseAsRaw()throws Exception{
            Source source = Okio.source(meta.getDataFile());
            BufferedSource bs = Okio.buffer(source);
            bs.skip(dataOffset);
            String str;
            if(showMode == MODE_HEX){
                try {
                    byte[] data = bs.readByteArray(Config.MAX_LOAD);
                    str = StrUtil.convertHexStr(data, 0, data.length);
                } catch (EOFException e) {
                    IOUtils.safeClose(bs);
                    source = Okio.source(meta.getDataFile());
                    bs = Okio.buffer(source);
                    bs.skip(dataOffset);
                    byte[] data = bs.readByteArray();
                    str = StrUtil.convertHexStr(data, 0, data.length);
                }
            }else{
                try {
                    str = bs.readByteString(Config.MAX_LOAD)
                            .string(Charset.defaultCharset());
                } catch (EOFException e) {
                    IOUtils.safeClose(bs);
                    source = Okio.source(meta.getDataFile());
                    bs = Okio.buffer(source);
                    bs.skip(dataOffset);
                    str = bs.readByteString()
                            .string(Charset.defaultCharset());
                }
            }
            boolean hasMore = !bs.exhausted();
            IOUtils.safeClose(bs);
            String retStr = str;
            tv_headers.post(() ->{
                if(pos == getAdapterPosition()) {
                    if(dataOffset == 0){
                        tv_headers.setText(retStr);
                        dataOffset = Config.MAX_LOAD;
                    }else{
                        dataOffset += Config.MAX_LOAD;
                        String s = tv_headers.getText().toString();
                        if(!TextUtils.isEmpty(s)){
                            tv_headers.setText(s+retStr);
                        }else{
                            tv_headers.setText(retStr);
                        }
                    }

                    tv_content.setVisibility(View.GONE);
                    if(hasMore){
                        iv_image.setVisibility(View.VISIBLE);
                        iv_image.setEnabled(true);
                        if(meta.isSend()){
                            iv_image.setImageResource(R.drawable.ic_drop_more);
                        }else{
                            iv_image.setImageResource(R.drawable.ic_drop_more_hl);
                        }
                    }else{
                        iv_image.setVisibility(View.GONE);
                    }
                }
            });
        }


        private void parseAsHttp() throws Exception{
            Source source = Okio.source(meta.getDataFile());
            BufferedSource bs = Okio.buffer(source);
            String line = bs.readUtf8LineStrict(4096);
            StringBuilder headerBuilder = new StringBuilder();
            String contentType = null;
            String contentEncoding = null;
            while (line != null && line.length() > 0){
                String[] keys = line.split(":");
                if("Content-Type".equals(keys[0])){
                    contentType = keys[1].trim().toLowerCase();
                }else if("Content-Encoding".equals(keys[0])){
                    contentEncoding = keys[1].trim().toLowerCase();
                }
                headerBuilder.append(line).append("\n");
                line = bs.readUtf8LineStrict(4096);
            }

            tv_headers.post(() ->{
                if(pos == getAdapterPosition()) {
                    tv_headers.setText(headerBuilder.toString());
                }
            });
            byte[] data;
            if("gzip".equals(contentEncoding)){
                bs.readUtf8LineStrict();
                GzipSource gs = new GzipSource(bs);
                BufferedSource gbs = Okio.buffer(gs);
                Buffer buffer = new Buffer();
                try {
                    while (true) {
                        byte b = gbs.readByte();
                        buffer.writeByte(b);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                data = buffer.readByteArray();
                buffer.close();
                gbs.close();
            }else{
                data = bs.readByteArray();
            }

            if(data != null && data.length <=0){
                return;
            }

            if(contentType != null && contentType.contains("image")){
                tv_headers.post(()->{
                    tv_content.setVisibility(View.GONE);
                    iv_image.setVisibility(View.VISIBLE);
                    iv_image.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                });
            }else{
                String content = new String(data);
                tv_headers.post(() ->{
                    if(pos == getAdapterPosition()) {
                        tv_content.setVisibility(View.VISIBLE);
                        tv_content.setText(content);
                        iv_image.setVisibility(View.GONE);
                    }
                });
            }
        }


        @Override
        public void run() {
            try {
                parse();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
