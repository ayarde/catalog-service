package com.ecommerce.catalog.boot.config;

import com.ecommerce.catalog.application.dto.ActivateProductCommand;
import com.ecommerce.catalog.application.dto.CreateProductCommand;
import com.ecommerce.catalog.application.dto.ImageRequest;
import com.ecommerce.catalog.application.dto.VariantRequest;
import com.ecommerce.catalog.application.port.in.ActivateProductUseCase;
import com.ecommerce.catalog.application.port.in.CreateProductUseCase;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CreateProductUseCase createProductUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final ProductRepository productRepository;

    public DataSeeder(CreateProductUseCase createProductUseCase,
                      ActivateProductUseCase activateProductUseCase,
                      ProductRepository productRepository) {
        this.createProductUseCase = createProductUseCase;
        this.activateProductUseCase = activateProductUseCase;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.findAll(0, 1).totalElements() > 0) {
            log.info("Database already contains products. Skipping seed.");
            return;
        }

        log.info("Seeding catalog with sample products...");
        seedIPhone16ProMax();
        seedMacBookPro16();
        seedSonyWH1000XM6();
        log.info("Catalog seeding completed successfully.");
    }

    private void seedIPhone16ProMax() {
        var product = createProductUseCase.create(new CreateProductCommand(
                "SKU-APH-IP16PM-001",
                "iPhone 16 Pro Max",
                "El iPhone más potente jamás creado. Con el chip A18 Pro, cámara Fusion de 48MP, " +
                "zoom óptico 5x y diseño en titanio. Perfecto para creadores de contenido y usuarios " +
                "que exigen lo mejor en rendimiento móvil.",
                new BigDecimal("1799.00"),
                "USD",
                List.of("Smartphones", "Apple", "Premium"),
                List.of("5G", "Nuevo", "Pro", "Titanio"),
                Map.of(
                        "Pantalla", "6.9\" Super Retina XDR OLED - 2868x1320px",
                        "Procesador", "A18 Pro (3nm) - Neural Engine 16-core",
                        "Cámara Trasera", "48MP Fusion + 48MP Ultra Wide + 12MP Telephoto 5x",
                        "Cámara Frontal", "12MP TrueDepth con enfoque automático",
                        "Batería", "Hasta 33h reproducción de video",
                        "Almacenamiento", "Desde 256GB hasta 1TB",
                        "Peso", "227g",
                        "Resistencia", "IP68 (6m hasta 30 min)",
                        "Conectividad", "5G, Wi-Fi 7, Bluetooth 5.4, USB-C"
                ),
                List.of(
                        new VariantRequest("SKU-APH-IP16PM-256-NT", "Titanio Natural / 256GB",
                                new BigDecimal("1799.00"), "USD", 25,
                                Map.of("Color", "Titanio Natural", "Almacenamiento", "256GB")),
                        new VariantRequest("SKU-APH-IP16PM-512-NT", "Titanio Natural / 512GB",
                                new BigDecimal("1999.00"), "USD", 15,
                                Map.of("Color", "Titanio Natural", "Almacenamiento", "512GB")),
                        new VariantRequest("SKU-APH-IP16PM-1TB-NT", "Titanio Natural / 1TB",
                                new BigDecimal("2299.00"), "USD", 10,
                                Map.of("Color", "Titanio Natural", "Almacenamiento", "1TB")),
                        new VariantRequest("SKU-APH-IP16PM-256-NB", "Titanio Negro / 256GB",
                                new BigDecimal("1799.00"), "USD", 30,
                                Map.of("Color", "Titanio Negro", "Almacenamiento", "256GB")),
                        new VariantRequest("SKU-APH-IP16PM-512-NB", "Titanio Negro / 512GB",
                                new BigDecimal("1999.00"), "USD", 20,
                                Map.of("Color", "Titanio Negro", "Almacenamiento", "512GB"))
                ),
                List.of(
                        new ImageRequest("https://images.unsplash.com/photo-1721572337245-0d5c5d0e1e0a?w=800",
                                "iPhone 16 Pro Max en titanio natural visto de frente", 1),
                        new ImageRequest("https://images.unsplash.com/photo-1721572337192-7e0b3a5e9c9e?w=800",
                                "iPhone 16 Pro Max sistema de tres cámaras trasero", 2)
                )
        ));

        activateProductUseCase.activate(new ActivateProductCommand(product.id()));
        log.info("Seeded: iPhone 16 Pro Max (ID={})", product.id());
    }

    private void seedMacBookPro16() {
        var product = createProductUseCase.create(new CreateProductCommand(
                "SKU-APL-MBP16-001",
                "MacBook Pro 16\" M4 Max",
                "La máxima potencia para profesionales. Con el chip M4 Max de hasta 16 núcleos " +
                "de CPU y 40 núcleos de GPU, pantalla Liquid Retina XDR de 16.2 pulgadas y " +
                "hasta 128GB de memoria unificada. Diseñada para desarrolladores, creadores " +
                "multimedia y científicos de datos.",
                new BigDecimal("3499.00"),
                "USD",
                List.of("Laptops", "Apple", "Profesional"),
                List.of("M4", "Pro", "Trabajo", "Desarrollo"),
                Map.of(
                        "Pantalla", "16.2\" Liquid Retina XDR - 3456x2234px - ProMotion 120Hz",
                        "Procesador", "Apple M4 Max - CPU 16-core, GPU 40-core, Neural Engine 16-core",
                        "RAM", "Hasta 128GB memoria unificada (LPDDR5x)",
                        "Almacenamiento", "SSD hasta 4TB con velocidad de lectura 7.4GB/s",
                        "Puertos", "3x Thunderbolt 5, HDMI 2.1, SDXC, MagSafe 3, Jack 3.5mm",
                        "Batería", "Hasta 22h reproducción de video",
                        "Peso", "2.14kg",
                        "Conectividad", "Wi-Fi 6E, Bluetooth 5.3",
                        "Cámara", "FaceTime HD 1080p con procesador de señal avanzado"
                ),
                List.of(
                        new VariantRequest("SKU-APL-MBP16-36-1TB", "M4 Max / 36GB / 1TB SSD",
                                new BigDecimal("3499.00"), "USD", 12,
                                Map.of("Memoria", "36GB", "Almacenamiento", "1TB SSD")),
                        new VariantRequest("SKU-APL-MBP16-48-1TB", "M4 Max / 48GB / 1TB SSD",
                                new BigDecimal("3999.00"), "USD", 8,
                                Map.of("Memoria", "48GB", "Almacenamiento", "1TB SSD")),
                        new VariantRequest("SKU-APL-MBP16-64-2TB", "M4 Max / 64GB / 2TB SSD",
                                new BigDecimal("4499.00"), "USD", 5,
                                Map.of("Memoria", "64GB", "Almacenamiento", "2TB SSD")),
                        new VariantRequest("SKU-APL-MBP16-128-4TB", "M4 Max / 128GB / 4TB SSD",
                                new BigDecimal("4999.00"), "USD", 3,
                                Map.of("Memoria", "128GB", "Almacenamiento", "4TB SSD"))
                ),
                List.of(
                        new ImageRequest("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800",
                                "MacBook Pro 16\" en espacio gris vista frontal abierta", 1),
                        new ImageRequest("https://images.unsplash.com/photo-1611186871348-b1f696febbb3?w=800",
                                "MacBook Pro 16\" lateral con puertos Thunderbolt", 2)
                )
        ));

        activateProductUseCase.activate(new ActivateProductCommand(product.id()));
        log.info("Seeded: MacBook Pro 16\" M4 Max (ID={})", product.id());
    }

    private void seedSonyWH1000XM6() {
        var product = createProductUseCase.create(new CreateProductCommand(
                "SKU-SNY-WH1KXM6-001",
                "Sony WH-1000XM6",
                "Los auriculares inalámbricos con cancelación de ruido líder en el mundo. " +
                "Con el procesador QN2e, audio de alta resolución inalámbrico, hasta 40 horas " +
                "de batería y comodidad superior para largas sesiones de uso. Ideales para " +
                "viajeros, profesionales y audiófilos.",
                new BigDecimal("449.00"),
                "USD",
                List.of("Audio", "Sony", "Premium"),
                List.of("Inalámbrico", "Cancelación Ruido", "Hi-Res", "Bluetooth"),
                Map.of(
                        "Tipo", "Over-Ear cerrado",
                        "Driver", "30mm libre de oxígeno de cobre",
                        "Cancelación de Ruido", "Activa Adaptive Sound Control QN2e",
                        "Códecs", "LDAC, AAC, SBC",
                        "Respuesta en Frecuencia", "4Hz - 40kHz (con cable)",
                        "Batería", "Hasta 40h con ANC activado",
                        "Carga Rápida", "3 minutos = 3 horas de reproducción",
                        "Peso", "250g",
                        "Multipunto", "Conexión simultánea a 2 dispositivos",
                        "Resistencia", "No certificado (no recomendado para lluvia intensa)"
                ),
                List.of(
                        new VariantRequest("SKU-SNY-WH1KXM6-BLK", "Negro",
                                new BigDecimal("449.00"), "USD", 50,
                                Map.of("Color", "Negro")),
                        new VariantRequest("SKU-SNY-WH1KXM6-SIL", "Plateado",
                                new BigDecimal("449.00"), "USD", 35,
                                Map.of("Color", "Plateado")),
                        new VariantRequest("SKU-SNY-WH1KXM6-BLU", "Azul Medianoche",
                                new BigDecimal("449.00"), "USD", 20,
                                Map.of("Color", "Azul Medianoche"))
                ),
                List.of(
                        new ImageRequest("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800",
                                "Sony WH-1000XM6 negro visto en ángulo", 1),
                        new ImageRequest("https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800",
                                "Sony WH-1000XM6 plegado con estuche", 2)
                )
        ));

        activateProductUseCase.activate(new ActivateProductCommand(product.id()));
        log.info("Seeded: Sony WH-1000XM6 (ID={})", product.id());
    }
}
