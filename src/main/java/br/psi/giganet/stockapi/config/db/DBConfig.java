package br.psi.giganet.stockapi.config.db;

import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.common.notifications.repository.NotificationRepository;
import br.psi.giganet.stockapi.common.valid_mac_addresses.repository.ValidMacAddressesRepository;
import br.psi.giganet.stockapi.config.audit.AuditorAwareImpl;
import br.psi.giganet.stockapi.config.security.repository.AbstractUserRepository;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import br.psi.giganet.stockapi.dashboard.main_items.repository.MainDashboardItemGroupRepository;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.entries.repository.EntryRepository;
import br.psi.giganet.stockapi.moves_request.repository.MovesRequestRepository;
import br.psi.giganet.stockapi.nfe.repository.NFeContentRepository;
import br.psi.giganet.stockapi.nfe.repository.NFeEntryItemRepository;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyMoveRepository;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyRepository;
import br.psi.giganet.stockapi.patrimonies_locations.repository.PatrimonyLocationRepository;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderItemRepository;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.stockapi.sellers.repository.SellerRepository;
import br.psi.giganet.stockapi.schedules.repository.ScheduledMoveRepository;
import br.psi.giganet.stockapi.stock.repository.StockItemQuantityLevelRepository;
import br.psi.giganet.stockapi.stock.repository.StockItemRepository;
import br.psi.giganet.stockapi.stock.repository.StockRepository;
import br.psi.giganet.stockapi.stock_moves.repository.StockMovesRepository;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.technician.technician_product_category.repository.TechnicianSectorProductCategoryRepository;
import br.psi.giganet.stockapi.templates.repository.TemplateRepository;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.ZonedDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@EnableJpaRepositories(basePackageClasses = {
        AbstractUserRepository.class,
        PermissionRepository.class,
        ProductRepository.class,
        EmployeeRepository.class,
        PurchaseOrderRepository.class,
        EntryRepository.class,
        NFeContentRepository.class,
        NFeEntryItemRepository.class,
        PurchaseOrderItemRepository.class,
        UnitRepository.class,
        ProductCategoryRepository.class,
        StockRepository.class,
        StockItemRepository.class,
        StockMovesRepository.class,
        TechnicianRepository.class,
        SellerRepository.class,
        PatrimonyRepository.class,
        ScheduledMoveRepository.class,
        TemplateRepository.class,
        PatrimonyLocationRepository.class,
        PatrimonyMoveRepository.class,
        ValidMacAddressesRepository.class,
        MovesRequestRepository.class,
        StockItemQuantityLevelRepository.class,
        TechnicianSectorProductCategoryRepository.class,
        NotificationRepository.class,
        MainDashboardItemGroupRepository.class,
        BranchOfficeRepository.class
})

public class DBConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(ZonedDateTime.now());
    }
}
