/**
 * This file is part of Linked Map Service (LMS).
 *
 * Linked Map Service (LMS) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linked Map Service (LMS) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Linked Map Service (LMS).  If not, see <http://www.gnu.org/licenses/>.
 */
package es.unizar.iaaa.lms.web.view;

import static es.unizar.iaaa.lms.util.Util.mergeHeaders;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.thymeleaf.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.ProcessingContext;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.fragment.ChainedFragmentSpec;
import org.thymeleaf.fragment.IFragmentSpec;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.dialect.SpringStandardDialect;
import org.thymeleaf.spring4.expression.ThymeleafEvaluationContext;
import org.thymeleaf.spring4.naming.SpringContextVariableNames;
import org.thymeleaf.spring4.view.AbstractThymeleafView;
import org.thymeleaf.spring4.view.ThymeleafView;
import org.thymeleaf.standard.expression.FragmentSelectionUtils;
import org.thymeleaf.standard.fragment.StandardFragment;
import org.thymeleaf.standard.fragment.StandardFragmentProcessor;
import org.thymeleaf.standard.processor.attr.StandardFragmentAttrProcessor;

public class HeadersAwareThymeleafView extends AbstractThymeleafView {

    public static final String HEADERS = "$HEADERS";

    /*
     * If this is not null, we are using Spring 3.1+ and there is the
     * possibility to automatically add @PathVariable's to models. This will be
     * computed at class initialization time.
     */
    private static final String pathVariablesSelector;

    private IFragmentSpec fragmentSpec = null;

    static {

        /*
         * Compute whether we can obtain @PathVariable's from the request and
         * add them automatically to the model (Spring 3.1+)
         */

        String pathVariablesSelectorValue = null;
        try {
            // We are looking for the value of the View.PATH_VARIABLES constant,
            // which is a String
            final Field pathVariablesField = View.class
                .getDeclaredField("PATH_VARIABLES");
            pathVariablesSelectorValue = (String) pathVariablesField.get(null);
        } catch (final NoSuchFieldException ignored) {
            pathVariablesSelectorValue = null;
        } catch (final IllegalAccessException ignored) {
            pathVariablesSelectorValue = null;
        }
        pathVariablesSelector = pathVariablesSelectorValue;
    }

    /**
     * <p>
     * Creates a new instance of <tt>HeadersAwareThymeleafView</tt>.
     * </p>
     */
    protected HeadersAwareThymeleafView() {
        super();
    }

    /**
     * <p>
     * Creates a new instance of <tt>HeadersAwareThymeleafView</tt>, specifying
     * the template name.
     * </p>
     * 
     * @param templateName
     *            the template name.
     */
    protected HeadersAwareThymeleafView(final String templateName) {
        super(templateName);
    }

    /**
     * <p>
     * Returns the fragment specification ({@link IFragmentSpec}) defining the
     * part of the template that should be processed.
     * </p>
     * <p>
     * This fragment spec will be used for selecting the section of the template
     * that should be processed, discarding the rest of the template. If null,
     * the whole template will be processed.
     * </p>
     * <p>
     * Subclasses of {@link ThymeleafView} might choose not to honor this
     * parameter, disallowing the processing of template fragments.
     * </p>
     * 
     * @return the fragment spec currently set, or null of no fragment has been
     *         specified yet.
     * 
     * @since 2.0.11
     */
    public IFragmentSpec getFragmentSpec() {
        return this.fragmentSpec;
    }

    /**
     * <p>
     * Sets the fragment specification ({@link IFragmentSpec}) defining the part
     * of the template that should be processed.
     * </p>
     * <p>
     * This fragment spec will be used for selecting the section of the template
     * that should be processed, discarding the rest of the template. If null,
     * the whole template will be processed.
     * </p>
     * <p>
     * Subclasses of {@link ThymeleafView} might choose not to honor this
     * parameter, disallowing the processing of template fragments.
     * </p>
     * 
     * @param fragmentSpec
     *            the fragment specification to be set.
     * 
     * @since 2.0.11
     */
    public void setFragmentSpec(final IFragmentSpec fragmentSpec) {
        this.fragmentSpec = fragmentSpec;
    }

    @Override
    public void render(final Map<String, ?> model,
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        renderFragment(null, model, request, response);
    }

    protected void renderFragment(final IFragmentSpec fragmentSpecToRender,
            final Map<String, ?> model, final HttpServletRequest request,
            final HttpServletResponse response)
            throws Exception {

        final ServletContext servletContext = getServletContext();
        final String viewTemplateName = getTemplateName();
        final TemplateEngine viewTemplateEngine = getTemplateEngine();
        if (!viewTemplateEngine.isInitialized()) {
            viewTemplateEngine.initialize();
        }

        if (viewTemplateName == null) {
            throw new IllegalArgumentException(
                "Property 'templateName' is required");
        }
        if (getLocale() == null) {
            throw new IllegalArgumentException("Property 'locale' is required");
        }

        final Map<String, Object> mergedModel = new HashMap<String, Object>(30);
        final Map<String, Object> templateStaticVariables = getStaticVariables();
        if (templateStaticVariables != null) {
            mergedModel.putAll(templateStaticVariables);
        }
        if (pathVariablesSelector != null) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> pathVars = (Map<String, Object>) request
                .getAttribute(pathVariablesSelector);
            if (pathVars != null) {
                mergedModel.putAll(pathVars);
            }
        }
        if (model != null) {
            mergedModel.putAll(model);
            
        }

        final ApplicationContext applicationContext = getApplicationContext();

        final RequestContext requestContext =
                new RequestContext(request, response, getServletContext(),
                    mergedModel);

        // For compatibility with ThymeleafView
        addRequestContextAsVariable(mergedModel,
            SpringContextVariableNames.SPRING_REQUEST_CONTEXT, requestContext);
        // For compatibility with AbstractTemplateView
        addRequestContextAsVariable(mergedModel,
            AbstractTemplateView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE,
            requestContext);

        // Expose Thymeleaf's own evaluation context as a model variable
        final ConversionService conversionService =
                (ConversionService) request
                    .getAttribute(ConversionService.class.getName()); // might
                                                                      // be
                                                                      // null!
        final ThymeleafEvaluationContext evaluationContext =
                new ThymeleafEvaluationContext(applicationContext,
                    conversionService);
        mergedModel
            .put(
                ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
                evaluationContext);

        final SpringWebContext context =
                new SpringWebContext(request, response, servletContext,
                    getLocale(), mergedModel, getApplicationContext());

        final String templateName;
        final IFragmentSpec nameFragmentSpec;
        if (!viewTemplateName.contains("::")) {
            // No fragment specified at the template name

            templateName = viewTemplateName;
            nameFragmentSpec = null;

        } else {
            // Template name contains a fragment name, so we should parse it as
            // such

            final Configuration configuration = viewTemplateEngine
                .getConfiguration();
            final ProcessingContext processingContext = new ProcessingContext(
                context);

            final String dialectPrefix = getStandardDialectPrefix(configuration);

            final StandardFragment fragment =
                    StandardFragmentProcessor.computeStandardFragmentSpec(
                        configuration, processingContext, viewTemplateName,
                        dialectPrefix, StandardFragmentAttrProcessor.ATTR_NAME);

            if (fragment == null) {
                throw new IllegalArgumentException(
                    "Invalid template name specification: '" + viewTemplateName
                            + "'");
            }

            templateName = fragment.getTemplateName();
            nameFragmentSpec = fragment.getFragmentSpec();
            final Map<String, Object> nameFragmentParameters = fragment
                .getParameters();

            if (nameFragmentParameters != null) {

                if (FragmentSelectionUtils
                    .parameterNamesAreSynthetic(nameFragmentParameters.keySet())) {
                    // We cannot allow synthetic parameters because there is no
                    // way to specify them at the template
                    // engine execution!
                    throw new IllegalArgumentException(
                        "Parameters in a view specification must be named (non-synthetic): '"
                                + viewTemplateName + "'");
                }

                context.setVariables(nameFragmentParameters);

            }

        }

        final String templateContentType = getContentType();
        final Locale templateLocale = getLocale();
        final String templateCharacterEncoding = getCharacterEncoding();

        IFragmentSpec templateFragmentSpec = fragmentSpecToRender;
        final IFragmentSpec viewFragmentSpec = getFragmentSpec();
        if (viewFragmentSpec != null) {
            if (templateFragmentSpec == null) {
                templateFragmentSpec = viewFragmentSpec;
            } else {
                templateFragmentSpec =
                        new ChainedFragmentSpec(viewFragmentSpec,
                            templateFragmentSpec);
            }
        }
        if (nameFragmentSpec != null) {
            if (templateFragmentSpec == null) {
                templateFragmentSpec = nameFragmentSpec;
            } else {
                templateFragmentSpec =
                        new ChainedFragmentSpec(nameFragmentSpec,
                            templateFragmentSpec);
            }
        }

        response.setLocale(templateLocale);
        if (templateContentType != null) {
            response.setContentType(templateContentType);
        } else {
            response.setContentType(DEFAULT_CONTENT_TYPE);
        }
        if (templateCharacterEncoding != null) {
            response.setCharacterEncoding(templateCharacterEncoding);
        }

        if (model.get(HEADERS) != null
                && model.get(HEADERS) instanceof HttpHeaders) {
            mergeHeaders(response, (HttpHeaders) model.get(HEADERS));
        }
        

        viewTemplateEngine.process(templateName, context, templateFragmentSpec,
            response.getWriter());

    }

    static String getStandardDialectPrefix(final Configuration configuration) {

        for (final Map.Entry<String, IDialect> dialectByPrefix : configuration
            .getDialects().entrySet()) {
            final IDialect dialect = dialectByPrefix.getValue();
            if (SpringStandardDialect.class
                .isAssignableFrom(dialect.getClass())) {
                return dialectByPrefix.getKey();
            }
        }

        throw new ConfigurationException(
            "StandardDialect dialect has not been found. In order to use AjaxThymeleafView, you should configure "
                    +
                    "the "
                    + SpringStandardDialect.class.getName()
                    + " dialect at your Template Engine");

    }

}
