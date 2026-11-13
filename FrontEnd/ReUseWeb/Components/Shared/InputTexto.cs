using Microsoft.AspNetCore.Components;
using Microsoft.AspNetCore.Components.Forms;
using Microsoft.AspNetCore.Components.Rendering;

namespace ReUseWeb.Components.Shared;

/// <summary>
/// InputText que valida mientras se escribe (evento oninput) en lugar de al salir del campo.
/// Así el campo se pone en rojo apenas queda inválido y el botón de envío (BotonEnviar)
/// se habilita en cuanto el último dato obligatorio está completo.
/// </summary>
public class InputTexto : InputText
{
    protected override void BuildRenderTree(RenderTreeBuilder builder)
    {
        builder.OpenElement(0, "input");
        builder.AddMultipleAttributes(1, AdditionalAttributes);
        builder.AddAttribute(2, "class", CssClass);
        builder.AddAttribute(3, "value", CurrentValueAsString);
        builder.AddAttribute(4, "oninput", EventCallback.Factory.CreateBinder<string?>(
            this, valor => CurrentValueAsString = valor, CurrentValueAsString));
        builder.SetUpdatesAttributeName("value");
        builder.AddElementReferenceCapture(5, referencia => Element = referencia);
        builder.CloseElement();
    }
}

/// <summary>InputTextArea que valida mientras se escribe (ver <see cref="InputTexto"/>).</summary>
public class AreaTexto : InputTextArea
{
    protected override void BuildRenderTree(RenderTreeBuilder builder)
    {
        builder.OpenElement(0, "textarea");
        builder.AddMultipleAttributes(1, AdditionalAttributes);
        builder.AddAttribute(2, "class", CssClass);
        builder.AddAttribute(3, "value", CurrentValueAsString);
        builder.AddAttribute(4, "oninput", EventCallback.Factory.CreateBinder<string?>(
            this, valor => CurrentValueAsString = valor, CurrentValueAsString));
        builder.SetUpdatesAttributeName("value");
        builder.AddElementReferenceCapture(5, referencia => Element = referencia);
        builder.CloseElement();
    }
}

/// <summary>InputNumber que valida mientras se escribe (ver <see cref="InputTexto"/>).</summary>
public class InputNumero<TValue> : InputNumber<TValue>
{
    protected override void BuildRenderTree(RenderTreeBuilder builder)
    {
        builder.OpenElement(0, "input");
        builder.AddAttribute(1, "step", "any");
        builder.AddMultipleAttributes(2, AdditionalAttributes);
        builder.AddAttribute(3, "type", "number");
        builder.AddAttribute(4, "class", CssClass);
        builder.AddAttribute(5, "value", CurrentValueAsString);
        builder.AddAttribute(6, "oninput", EventCallback.Factory.CreateBinder<string?>(
            this, valor => CurrentValueAsString = valor, CurrentValueAsString));
        builder.SetUpdatesAttributeName("value");
        builder.AddElementReferenceCapture(7, referencia => Element = referencia);
        builder.CloseElement();
    }
}
