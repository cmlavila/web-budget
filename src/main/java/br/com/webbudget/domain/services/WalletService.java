/*
 * Copyright (C) 2015 Arthur Gregorio, AG.Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.entities.registration.WalletBalance;
import br.com.webbudget.domain.entities.registration.BalanceType;
import br.com.webbudget.domain.events.UpdateBalance;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import br.com.webbudget.domain.services.misc.WalletBalanceBuilder;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import br.com.webbudget.domain.repositories.registration.WalletBalanceRepository;
import java.util.Optional;
import javax.enterprise.event.Observes;

/**
 * The service responsible for the business operations with {@link Wallet}
 *
 * @author Arthur Gregorio
 *
 * @version 1.2.0
 * @since 1.0.0, 12/03/2014
 */
@ApplicationScoped
public class WalletService {

    @Inject
    private WalletRepository walletRepository;
    @Inject
    private WalletBalanceRepository walletBalanceRepository;

    /**
     * Use this method to persist a {@link Wallet}
     *
     * @param wallet the {@link Wallet} to be persisted
     */
    @Transactional
    public void save(Wallet wallet) {

        final Optional<Wallet> found = this.walletRepository
                .findOptionalByNameAndWalletType(wallet.getName(), wallet.getWalletType());

        if (found.isPresent()) {
            throw new BusinessLogicException("error.wallet.duplicated");
        }

        wallet = this.walletRepository.save(wallet);

        final WalletBalanceBuilder builder = WalletBalanceBuilder.getInstance();

        builder.to(wallet)
                .withValue(wallet.getActualBalance())
                .withType(BalanceType.CREDIT);

        this.updateWalletBalance(builder);
    }

    /**
     * Use this method to update a persisted {@link Wallet}
     *
     * @param wallet the {@link Wallet} to be updated
     * @return the update {@link Wallet}
     */
    @Transactional
    public Wallet update(Wallet wallet) {

        final Optional<Wallet> found = this.walletRepository
                .findOptionalByNameAndWalletType(wallet.getName(), wallet.getWalletType());

        if (found.isPresent() && !found.get().equals(wallet)) {
            throw new BusinessLogicException("error.wallet.duplicated");
        }

        return this.walletRepository.save(wallet);
    }

    /**
     * Use this method to delete a persisted {@link Wallet}
     *
     * @param wallet the {@link Wallet} to be deleted
     */
    @Transactional
    public void delete(Wallet wallet) {
        this.walletRepository.attachAndRemove(wallet);
    }
    
    /**
     * Update the {@link WalletBalance} for a given wallet
     *
     * @param builder the builder with the balance historic
     */
    @Transactional
    public void updateWalletBalance(WalletBalanceBuilder builder) {

        final WalletBalance walletBalance = builder.build();

        // update the actual balance on the wallet
        this.walletRepository.save(walletBalance.getWallet());

        // save the new balance history
        this.walletBalanceRepository.save(walletBalance);
    }

    /**
     * This method listen to events on {@link UpdateBalance} and call the method
     * to update the balance based on the builder received as a parameter
     *
     * @param builder the {@link WalletBalanceBuilder}
     */
    @Transactional
    public void onWalletBalanceChange(@Observes @UpdateBalance WalletBalanceBuilder builder) {
        this.updateWalletBalance(builder);
    }
}
