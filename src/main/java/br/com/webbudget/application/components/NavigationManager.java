/*
 * Copyright (C) 2018 Arthur Gregorio, AG.Software
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
package br.com.webbudget.application.components;

import lombok.Getter;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds a mechanism to make the navigation though the pages more easy if you are working with default
 * CRUD operations and a default way of building pages
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 3.0.0, 13/02/2018
 */
public final class NavigationManager {

    private final Map<PageType, String> pages;

    /**
     * Private constructor to prevent misuse
     */
    private NavigationManager() {
        this.pages = new HashMap<>();
    }
    
    /**
     * Get the new instance for the page navigator
     * 
     * @return the navigation manager
     */
    public static NavigationManager getInstance() {
        return new NavigationManager();
    }

    /**
     * Add a page to the domain of the manager
     * 
     * @param pageType the {@link PageType} of the page to be add
     * @param page the string outcome of the page
     * @return if the current value to add is linked to other page, return it 
     */
    public String addPage(PageType pageType, String page) {
        return this.pages.putIfAbsent(pageType, page);
    }
    
    /**
     * Navigate to the specified page with the given parameters
     * 
     * @param pageType the {@link PageType} to navigate to
     * @param parameters the list of parameters to use on the outcome rule
     * @return the outcome rule to with the values
     */
    public String to(PageType pageType, Parameter... parameters) {
        
        final String root = this.pages.get(pageType);
        
        final StringBuilder builder = new StringBuilder(root);
        
        builder.append("?faces-redirect=true");
        
        for (Parameter parameter : parameters) {
            builder.append(parameter);
        }
        
        builder.append(Parameter.of("viewState", pageType.getViewState()));
        
        return builder.toString();
    }
    
    /**
     * Make a redirect throug the external context to the given page
     * 
     * @param pageType the {@link PageType} to go
     * @param parameters the parameters to use on the redirect
     */
    public void redirect(PageType pageType, Parameter... parameters) {
       
        final String url = this.to(pageType, parameters);
        
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Can't redirect to url %s", url));
        }
    }
    
    /**
     * The enum to hold the possible values of pages types
     */
    public enum PageType {
        
        LIST_PAGE(ViewState.LISTING),
        DETAIL_PAGE(ViewState.DETAILING),
        ADD_PAGE(ViewState.ADDING),
        UPDATE_PAGE(ViewState.EDITING),
        DELETE_PAGE(ViewState.DELETING);
        
        @Getter
        private final ViewState viewState;

        /**
         * Each type need's to be linked with one {@link ViewState}
         * 
         * @param viewState the {@link ViewState} of the give parameter
         */
        PageType(ViewState viewState) {
            this.viewState = viewState;
        }
    }
    
    /**
     * The object to hold and construct the paramter values
     */
    public static class Parameter {

        @Getter
        private final String name;
        @Getter
        private final Object value;

        /**
         * Private constructor to prevent misuse
         * 
         * @param name the name of the parameter
         * @param value the value of the parameter
         */
        private Parameter(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Constructor...
         * 
         * @param name the name of the parameter
         * @param value the value of the parameter
         * @return the parameter object
         */
        public static Parameter of(String name, Object value) {
            return new Parameter(name, value);
        }
        
        /**
         * @return the value in the format of a URL parameter
         */
        @Override
        public String toString() {
            return "&" + this.name + "=" + this.value;
        }
    }
}
