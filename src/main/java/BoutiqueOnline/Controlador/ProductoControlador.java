package BoutiqueOnline.Controlador;

import BoutiqueOnline.Reportes.ReporteExcelServicio;
import BoutiqueOnline.modelo.Producto;
import BoutiqueOnline.modelo.Usuario;
import BoutiqueOnline.servicio.ProductoServicio;
import BoutiqueOnline.servicio.UploadFileService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/productos")
public class ProductoControlador {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductoControlador.class);

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private UploadFileService upload;

    @Autowired
    private ReporteExcelServicio reporteExcel;

    @GetMapping("/gestionProducto")
    public String VistaProducto(Model model) {
        List<Producto> productos = productoServicio.findAll();
        model.addAttribute("productos", productos);

        return "productos/gestionProducto";
    }

    @GetMapping("/show")
    public String show(Model model) {
        model.addAttribute("productos", productoServicio.findAll());
        return "productos/show";
    }

    @GetMapping("/create")
    public String create() {
        return "productos/create";
    }

    @PostMapping("/save")
    public String save(Producto producto, @RequestParam("img") MultipartFile file) throws IOException {
        LOGGER.info("Este es el objeto producto {} ", producto);
        Usuario u = new Usuario(2, "", "", "", "", "");
        producto.setUsuario(u);

        //logica para subir la imagen
        if (producto.getId() == null) {//cuando la imagen se sube por primera vez
            String nombreImagen = upload.saveImages(file);
            producto.setImagen(nombreImagen);
        } else {//para editar imagnes

        }

        productoServicio.save(producto);
        return "redirect:/productos/show";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Producto producto = new Producto();
        Optional<Producto> optionalProducto = productoServicio.get(id);
        producto = optionalProducto.get();

        LOGGER.info("Producto buscado: {}", producto);
        model.addAttribute("producto", producto);

        return "productos/edit";
    }

    @PostMapping("/update")
    public String updale(Producto producto, @RequestParam("img") MultipartFile file) throws IOException {

        Producto p = new Producto();
        p = productoServicio.get(producto.getId()).get();

        if (file.isEmpty()) {//cuando se edita la misma imagen y se carga esa imagen
            producto.setImagen((p.getImagen()));
        } else {//cuando queremos editar la imagen
            //para eliminar cuando no sea la imagen por defecto
            if (!p.getImagen().equals("default.jpg")) {
                upload.deleteImagen(p.getImagen());
            }

            producto.setUsuario(p.getUsuario());
            String nombreImagen = upload.saveImages(file);
            producto.setImagen(nombreImagen);
        }

        productoServicio.update(producto);
        return "redirect:/productos/show";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        Producto p = new Producto();
        p = productoServicio.get(id).get();

        //para eliminar cuando no sea la imagen por defecto
        if (!p.getImagen().equals("default.jpg")) {
            upload.deleteImagen(p.getImagen());
        }

        productoServicio.delete(id);
        return "redirect:/productos/show";
    }

    //endpoint para producto 
    @GetMapping("/reporte")
    public ResponseEntity<byte[]> generarReporte() throws IOException {
        List<Producto> productos = productoServicio.findAll();
        ByteArrayInputStream excelStream = reporteExcel.generarReporteExcel(productos);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_productos.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelStream.readAllBytes());
    }
}
