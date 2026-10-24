import java.awt.image.*;import javax.imageio.ImageIO;import java.nio.file.*;import java.util.*;
public class RefreshTextures {
 static BufferedImage twice(BufferedImage s){int w=s.getWidth(),h=s.getHeight();BufferedImage o=new BufferedImage(w*2,h*2,2);for(int y=0;y<h;y++)for(int x=0;x<w;x++){int e=s.getRGB(x,y),b=s.getRGB(x,Math.max(0,y-1)),d=s.getRGB(Math.max(0,x-1),y),f=s.getRGB(Math.min(w-1,x+1),y),q=s.getRGB(x,Math.min(h-1,y+1));int[] v={d==b&&d!=q&&b!=f?d:e,b==f&&b!=d&&f!=q?f:e,d==q&&d!=b&&q!=f?d:e,q==f&&d!=q&&b!=f?f:e};for(int n=0;n<4;n++)o.setRGB(x*2+n%2,y*2+n/2,v[n]);}return o;}
 public static void main(String[] args)throws Exception{
  Path root=Path.of("src/main/resources/assets/ic2r_addons/textures");
  try(var files=Files.walk(root)){for(Path p:files.filter(p->p.toString().endsWith(".png")&&!p.toString().contains("gui")).toList()){
   String oldPath=p.toString().replace('\\','/').replace("ic2r_addons/","solar_gravisuite/");
   Process original=new ProcessBuilder("git","show","64b4c20:"+oldPath).redirectError(ProcessBuilder.Redirect.DISCARD).start();
   byte[] bytes=original.getInputStream().readAllBytes(); original.waitFor();
   BufferedImage s=bytes.length>0?ImageIO.read(new java.io.ByteArrayInputStream(bytes)):ImageIO.read(Path.of("tools/textures/engine-original/"+p.getFileName()).toFile());BufferedImage out=twice(s);
   if(p.toString().contains("armor")||p.getFileName().toString().contains("helmet")){for(int y=0;y<out.getHeight();y++)for(int x=0;x<out.getWidth();x++){int c=out.getRGB(x,y),r=c>>16&255,g=c>>8&255,b=c&255;if(g>r+25&&g>b+25){out.setRGB(x,y,(c&0xff000000)|(r<<16)|((int)(g*.7)<<8)|g);}}}
   ImageIO.write(out,"png",p.toFile());
  }}
  BufferedImage atlas=ImageIO.read(Path.of("tools/textures/1.2.0-tools-generated.png").toFile());
  String[] names={"advanced_diamond_drill","advanced_iridium_drill","advanced_chainsaw","gravitool/wrench"};
  // Resample the generated atlas at the centers of a strict 32-pixel inventory grid.
  for(int i=0;i<4;i++){BufferedImage out=new BufferedImage(32,32,2);int cw=atlas.getWidth()/2,ch=atlas.getHeight()/2;for(int y=0;y<32;y++)for(int x=0;x<32;x++){int c=atlas.getRGB((i%2)*cw+(2*x+1)*cw/64,(i/2)*ch+(2*y+1)*ch/64);if((c>>>24)<128)c=0;else c|=0xff000000;out.setRGB(x,y,c);}Path p=root.resolve("item/"+names[i]+".png");if(!Files.exists(p)&&i==3){try(var files=Files.list(root.resolve("item/gravitool"))){p=files.filter(f->f.toString().endsWith(".png")).findFirst().orElseThrow();}}ImageIO.write(out,"png",p.toFile());}
  BufferedImage equipment=ImageIO.read(Path.of("tools/textures/1.2.0-equipment-generated.png").toFile());
  String[] eq={"advsolarhelmet","armor/advanced_nano","advanced_nano_leggings","advanced_nano_boots","hybridsolarhelmet","ultimatesolarhelmet","armor/advanced_quant","advanced_quantum_leggings","advanced_quantum_boots","armor/advanced_jetpack","armor/advanced_lappack","armor/ultimate_lappack","vajra","magnet","voider","relocator"};
  for(int i=0;i<eq.length;i++){
   BufferedImage icon=new BufferedImage(32,32,2);int cellW=equipment.getWidth()/4,cellH=equipment.getHeight()/4;
   for(int y=0;y<32;y++)for(int x=0;x<32;x++){
    int c=equipment.getRGB((i%4)*cellW+(2*x+1)*cellW/64,(i/4)*cellH+(2*y+1)*cellH/64);
    if((c>>>24)<128)c=0;else c|=0xff000000;
    if(i<12){int red=c>>16&255,green=c>>8&255,blue=c&255;if(green>red+25&&green>blue+25)c=(c&0xff000000)|(red<<16)|((int)(green*.7)<<8)|green;}
    icon.setRGB(x,y,c);
   }
   ImageIO.write(icon,"png",root.resolve("item/"+eq[i]+".png").toFile());
  }
 }
}
