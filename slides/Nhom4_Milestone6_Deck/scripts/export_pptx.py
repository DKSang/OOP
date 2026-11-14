#!/usr/bin/env python3
"""Export an anthropic-design-system HTML deck to an EDITABLE .pptx (native text boxes + shapes).

  python scripts/export_pptx.py path/to/deck.html --out deck.pptx [--notes] [--keep-svg]

How: Playwright renders the deck, a prep step flattens the things PowerPoint cannot express
(icon <use> sprites, CSS-cropped screenshots, grain-filtered illustrations) into PNG/inline SVG, then
dom-to-pptx (MIT, https://github.com/atharva9167j/dom-to-pptx) maps computed styles to PowerPoint objects.
Needs: python playwright, node + `npm i dom-to-pptx` (auto-installed into scripts/.pptx_node on first run).
Fonts: PowerPoint has no Literata / Hanken Grotesk / JetBrains Mono unless installed; fonts/ ships OFL TTF-less woff2,
so install the families once on the presenting machine, or text falls back to Arial.
"""
import argparse, base64, http.server, os, pathlib, subprocess, sys, threading, functools
HERE = pathlib.Path(__file__).resolve().parent
NODE = pathlib.Path(os.environ.get('PPTX_NODE_DIR') or HERE / '.pptx_node')
PREP = r"""
async () => {
  const slides=[...document.querySelectorAll('.ad-slide')];
  const toPng=(w,h,draw,scale=2)=>{const c=document.createElement('canvas');c.width=w*scale;c.height=h*scale;draw(c.getContext('2d'),scale);return c.toDataURL('image/png')};
  const load=src=>new Promise((res,rej)=>{const i=new Image();i.onload=()=>res(i);i.onerror=rej;i.src=src});
  // 1) expand <use href="#id"> icon references into inline shapes (sprite is not visible to the exporter)
  document.querySelectorAll('svg use').forEach(u=>{
    const id=(u.getAttribute('href')||'').slice(1); const sym=document.getElementById(id); if(!sym) return;
    const g=document.createElementNS('http://www.w3.org/2000/svg','g'); g.innerHTML=sym.innerHTML; u.replaceWith(g);});
  // 2) CSS-cropped screenshots -> one flat PNG of exactly what is visible
  for (const box of document.querySelectorAll('.ad-shot__view, .ad-zoom')) {
    const img=box.querySelector('img'); if(!img) continue;
    const W=box.clientWidth,H=box.clientHeight; const L=parseFloat(img.style.left)||0,T=parseFloat(img.style.top)||0,IW=parseFloat(img.style.width),IH=parseFloat(img.style.height);
    const src=await load(img.src);
    const url=toPng(W,H,(x,s)=>x.drawImage(src,L*s,T*s,IW*s,IH*s),2);
    const n=document.createElement('img'); n.src=url; n.style.cssText=`position:static;display:block;width:${W}px;height:${H}px;max-width:none`; img.replaceWith(n);
  }
  // 3) grain-filtered illustrations / cover art -> PNG (filters do not survive as vector)
  for (const s of document.querySelectorAll('svg')) {
    if(!(s.innerHTML.includes('url(#grain)'))) continue;
    const r=s.getBoundingClientRect(); const W=Math.round(r.width),H=Math.round(r.height); if(!W||!H) continue;
    const c=s.cloneNode(true); c.setAttribute('xmlns','http://www.w3.org/2000/svg'); c.setAttribute('width',W); c.setAttribute('height',H);
    const src=await load('data:image/svg+xml;charset=utf-8,'+encodeURIComponent(new XMLSerializer().serializeToString(c)));
    const url=toPng(W,H,(x,sc)=>x.drawImage(src,0,0,W*sc,H*sc),2);
    const n=document.createElement('img'); n.src=url; const cs=getComputedStyle(s);
    n.style.cssText=s.getAttribute('style')||''; n.className=s.getAttribute('class')||''; n.style.width=W+'px'; n.style.height=H+'px'; s.replaceWith(n);
  }
  // 4) PowerPoint text boxes wrap earlier than browsers: give single-line labels slack, and neutralise negative tracking
  document.querySelectorAll('.ad-note,.ad-tag,.ad-pill,.ad-chip,.ad-row__tag,.ad-tl__label,.ad-step__cap,.ad-ui-meta').forEach(e=>{const w=e.getBoundingClientRect().width; e.style.boxSizing='content-box'; e.style.paddingRight=(parseFloat(getComputedStyle(e).paddingRight)+Math.max(14,w*.12))+'px'; e.style.whiteSpace='nowrap'});
  document.querySelectorAll('.ad-thanks__title,.ad-title,.ad-statement,.ad-cover__title').forEach(e=>{e.style.letterSpacing='0'; e.style.wordSpacing='0'});
  return slides.length;
}
"""
EXPORT = r"""
async (opts) => {
  const els=[...document.querySelectorAll('.ad-slide')];
  els.forEach((e,i)=>{ if(opts.notes && !e.dataset.pptxNotes){ const t=e.getAttribute('aria-label'); if(t) e.dataset.pptxNotes=t; }});
  const blob=await window.domToPptx.exportToPptx(els,{svgAsVector:opts.svg,skipDownload:true});
  const u=new Uint8Array(await blob.arrayBuffer()); let s=''; for(let i=0;i<u.length;i+=32768) s+=String.fromCharCode.apply(null,u.subarray(i,i+32768)); return btoa(s);
}
"""
def ensure_node():
    if not (NODE/'node_modules/dom-to-pptx').exists():
        NODE.mkdir(exist_ok=True); subprocess.check_call(['npm','init','-y'],cwd=NODE,stdout=subprocess.DEVNULL)
        subprocess.check_call(['npm','install','dom-to-pptx'],cwd=NODE)
    return (NODE/'node_modules/dom-to-pptx/dist/dom-to-pptx.bundle.js').read_text()
def main():
    ap=argparse.ArgumentParser(); ap.add_argument('html'); ap.add_argument('--out',default='deck.pptx')
    ap.add_argument('--notes',action='store_true',help='slide aria-label as speaker notes'); ap.add_argument('--keep-svg',action='store_true',help='keep remaining SVG as vectors')
    a=ap.parse_args()
    from playwright.sync_api import sync_playwright
    html=pathlib.Path(a.html).resolve(); ds=HERE.parent
    root=pathlib.Path(os.path.commonpath([html.parent,ds])); rel=html.relative_to(root).as_posix()
    bundle=ensure_node()
    H=functools.partial(http.server.SimpleHTTPRequestHandler,directory=str(root)); H.log_message=lambda *x:None
    srv=http.server.ThreadingHTTPServer(('127.0.0.1',0),H); threading.Thread(target=srv.serve_forever,daemon=True).start()
    url=f'http://127.0.0.1:{srv.server_port}/{rel}'
    with sync_playwright() as p:
        b=p.chromium.launch(); pg=b.new_page(viewport={'width':1920,'height':1080}); errs=[]
        pg.on('pageerror',lambda e:errs.append(str(e)[:200])); pg.goto(url); pg.wait_for_timeout(800)
        pg.evaluate("document.fonts.ready")
        pg.add_style_tag(content='html,body{overflow:visible!important;height:auto!important}.deck,.ad-deck{position:static!important;display:block!important}#stage,.ad-stage,.stage{transform:none!important;position:static!important;width:1920px!important;height:auto!important}.ad-slide{display:block!important;position:relative!important;width:1920px!important;height:1080px!important;inset:auto!important;margin:0!important}.hud{display:none!important}')
        # PowerPoint takes the FIRST family of a stack: point the tokens at the real OFL families (swap here if brand fonts are installed)
        pg.add_style_tag(content=":root{--ad-font-serif:'Literata',Georgia,serif!important;--ad-font-sans:'Hanken Grotesk',Arial,sans-serif!important;--ad-font-mono:'JetBrains Mono',Consolas,monospace!important}")
        pg.wait_for_timeout(300)
        n=pg.evaluate(PREP); pg.add_script_tag(content=bundle); pg.wait_for_timeout(300)
        data=pg.evaluate(EXPORT,{'notes':a.notes,'svg':a.keep_svg})
        pathlib.Path(a.out).write_bytes(base64.b64decode(data)); b.close()
    print(f'exported {n} slides -> {a.out}', ('; page errors: '+'; '.join(errs[:3])) if errs else '')
if __name__=='__main__': main()
